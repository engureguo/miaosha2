package com.engure.seckill.vo;

import com.engure.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsDetailVo {

    private GoodsVo goodsVo;
    private User user;
    private Integer remainSeconds;
    private Integer secKillStatus;

}
