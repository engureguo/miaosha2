package com.engure.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.engure.seckill.exception.GlobalException;
import com.engure.seckill.mapper.OrderMapper;
import com.engure.seckill.pojo.Order;
import com.engure.seckill.pojo.SeckillGoods;
import com.engure.seckill.pojo.SeckillOrder;
import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IOrderService;
import com.engure.seckill.service.ISeckillGoodsService;
import com.engure.seckill.service.ISeckillOrderService;
import com.engure.seckill.vo.GoodsVo;
import com.engure.seckill.vo.RespTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author engure
 * @since 2021-08-17
 */
@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Order seckill(User user, GoodsVo goodsVo) {

        //1减库存
        SeckillGoods goods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>()
                .eq("goods_id", goodsVo.getId()));//获取目标商品
        //goods.setStockCount(goods.getStockCount() - 1);//更新秒杀商品库存
        //seckillGoodsService.updateById(goods);
        boolean update = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count - 1")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0));
        if (!update)
            throw new GlobalException(RespTypeEnum.SECKILL_ERROR);

        //2生成订单
        Order order = new Order();
        //order.setId();  auto int
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(10001L);//邮寄地址编号
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goodsVo.getSeckillPrice());
        order.setOrderChannel(2);//下单渠道
        order.setStatus(0);//新建未支付
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());//generatedkey
        seckillOrder.setGoodsId(goods.getGoodsId());
        seckillOrderService.save(seckillOrder);

        //秒杀成功，将订单信息放入 redis，“很大程度上” 阻止用户多次购买
        redisTemplate.opsForValue().set("seckOrder:userId-" + user.getId()
                + ":goodsId-" + goods.getGoodsId(), order);

        return order;
    }

    /**
     * 查询秒杀订单
     *
     * @param userId
     * @param goodsId
     * @return >0订单id，=0秒杀中，<0失败
     */
    @Override
    public Long qrySeckillOrder(Long userId, Long goodsId) {

        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", userId)
                .eq("goods_id", goodsId));

        if (null != seckillOrder) {
            return seckillOrder.getOrderId();
        } else if (redisTemplate.hasKey("isSeckillGoodsEmpty:" + goodsId)) {
            return -1L;
        } else
            return 0L;
    }
}
