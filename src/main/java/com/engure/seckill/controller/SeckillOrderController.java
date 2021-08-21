package com.engure.seckill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.engure.seckill.pojo.Order;
import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IGoodsService;
import com.engure.seckill.service.IOrderService;
import com.engure.seckill.vo.GoodsVo;
import com.engure.seckill.vo.OrderDetailVo;
import com.engure.seckill.vo.RespBean;
import com.engure.seckill.vo.RespTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author engure
 * @since 2021-08-17
 */
@Controller
@RequestMapping("/killOrder")
public class SeckillOrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IGoodsService goodsService;

    /**
     * 根据普通订单的orderId找到普通订单
     *
     * @param orderId
     * @param user
     * @return
     */
    @PostMapping("/detail")
    @ResponseBody
    public RespBean detail(Long orderId, User user) {

        if (user == null) return RespBean.error(RespTypeEnum.SESSION_NOT_EXIST);

        //查出 商品和订单，返回数据
        Order order = orderService.getOne(new QueryWrapper<Order>()
                .eq("id", orderId)
                .eq("user_id", user.getId()));

        if (null == order)
            return RespBean.error(RespTypeEnum.ORDER_NOT_EXIST);

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrder(order);
        orderDetailVo.setGoodsVo(goodsVo);

        return RespBean.success(orderDetailVo);
    }

    /**
     * 查询秒杀结果
     *
     * @param goodsId
     * @param user
     * @return
     */
    @GetMapping("/qryOrder")
    @ResponseBody
    public RespBean qryOrder(Long goodsId, User user) {
        if (null == user) return RespBean.error(RespTypeEnum.SESSION_NOT_EXIST);

        Long orderId = orderService.qrySeckillOrder(user.getId(), goodsId);

        return RespBean.success(orderId);
    }

}
