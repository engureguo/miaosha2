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
    //校验
    VALIDATE_ERROR(500204, "参数校验错误"),

    ;

    private final int code;
    private final String msg;

}
