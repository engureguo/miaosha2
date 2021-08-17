package com.engure.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.engure.seckill.pojo.Goods;
import com.engure.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author engure
 * @since 2021-08-17
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVo> findAllGoodsVo();
}
