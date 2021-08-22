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
import com.engure.seckill.utils.MD5Util;
import com.engure.seckill.utils.UUIDUtil;
import com.engure.seckill.vo.GoodsVo;
import com.engure.seckill.vo.RespTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author engure
 * @since 2021-08-17
 */
@Service
@Slf4j
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

    private static final String SEKILL_PATH_KEY = "asdga;sigwekgj";

    /**
     * 创建秒杀接口
     *
     * @param user    用户信息
     * @param goodsId 商品信息
     * @return 用户 user 对于商品 goodsId 的秒杀路径，具有时效性
     */
    @Override
    public String createPath(User user, Long goodsId) {

        if (null == goodsId) throw new GlobalException(RespTypeEnum.GOODS_NOT_EXIST);

        String path = null;

        ValueOperations opsFV = redisTemplate.opsForValue();
        if (redisTemplate.hasKey("seckillPath:uid-" + user.getId() + ":gid-" + goodsId)) {
            path = (String) opsFV.get("seckillPath:uid-" + user.getId() + ":gid-" + goodsId);
        }
        if (null == path) {
            String uuid = UUIDUtil.uuid();
            path = MD5Util.md5(uuid + SEKILL_PATH_KEY);
            opsFV.set("seckillPath:uid-" + user.getId() + ":gid-" + goodsId, path,
                    60, TimeUnit.SECONDS);
        }

        return path;
    }

    /**
     * 检查秒杀接口
     *
     * @param user    非null
     * @param goodsId 非null
     * @param path    非null
     * @return
     */
    @Override
    public Boolean checkPath(User user, Long goodsId, String path) {

        String pathFromRedis =
                (String) redisTemplate.opsForValue().get("seckillPath:uid-" + user.getId() + ":gid-" + goodsId);

        return pathFromRedis != null && path.equals(pathFromRedis);
    }

    /**
     * 验证验证码
     *
     * @param user
     * @param goodsId
     * @param captcha
     * @return 1失效，2错误，3正确
     */
    @Override
    public Integer checkCaptcha(User user, Long goodsId, String captcha) {

        String captcha0 = (String) redisTemplate.opsForValue().get("captcha:uid-" + user.getId() + ":gid-" + goodsId);

        //log.info("captcha = ", captcha, captcha0);

        if (captcha0 == null) return 1;
        if (!captcha0.equals(captcha)) return 2;
        return 3;
    }
}
