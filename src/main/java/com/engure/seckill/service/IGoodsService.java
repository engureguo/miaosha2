package com.engure.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.engure.seckill.pojo.Goods;
import com.engure.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author engure
 * @since 2021-08-17
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 获取所有秒杀商品信息，返回 vo 列表
     *
     * @return
     */
    List<GoodsVo> findAllGoodsVo();

    /**
     * 获取秒杀商品信息，返回 vo 对象
     *
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
