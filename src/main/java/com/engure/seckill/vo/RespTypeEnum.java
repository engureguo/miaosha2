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
    USER_NOT_REGISTERED(500202,   "用户未注册"),
    LOGIN_SUCCESS(200, "登陆成功"),
    ALREADY_LOGIN(200, "已经登陆"),
    //校验
    VALIDATE_ERROR(500204, "参数校验错误"),
    //库存不足
    OUT_OF_STOCK(5005, "库存不足"),
    REPEATED_BUY_ERROR(5006, "超过购买次数"),

    ;

    private final int code;
    private final String msg;

}
