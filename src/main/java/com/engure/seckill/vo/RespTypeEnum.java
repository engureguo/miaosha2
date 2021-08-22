package com.engure.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RespTypeEnum {

    //常规
    SUCCESS(200, "请求成功"),
    ERROR(500, "服务端异常"),
    //登录
    LOGIN_ERROR(500201, "账号或密码错误"),
    USER_NOT_REGISTERED(500202, "用户未注册"),
    SESSION_NOT_EXIST(500203, "用户不存在"),
    LOGIN_SUCCESS(200, "登陆成功"),
    ALREADY_LOGIN(200, "已经登陆"),
    //校验
    VALIDATE_ERROR(500401, "参数校验错误"),
    //库存不足
    OUT_OF_STOCK(500501, "库存不足"),
    REPEATED_BUY_ERROR(500502, "超过购买次数"),
    //订单
    ORDER_NOT_EXIST(500601, "订单不存在"),
    //秒杀
    SECKILL_ERROR(500701, "秒杀失败，请重试~"),
    GOODS_NOT_EXIST(500702, "商品不存在~"),
    SECKILL_PATH_ERROR(500703, "秒杀路径错误"),

    ;

    private final int code;
    private final String msg;

}
