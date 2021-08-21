package com.engure.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.engure.seckill.pojo.Order;
import com.engure.seckill.pojo.User;
import com.engure.seckill.vo.GoodsVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author engure
 * @since 2021-08-17
 */
public interface IOrderService extends IService<Order> {

    /**
     * 秒杀业务
     *
     * @param user
     * @param goodsVo
     * @return
     */
    Order seckill(User user, GoodsVo goodsVo);

    /**
     * 查询秒杀订单
     *
     * @param id
     * @param goodsId
     * @return >0订单id，=0秒杀中，<0失败
     */
    Long qrySeckillOrder(Long id, Long goodsId);
}
