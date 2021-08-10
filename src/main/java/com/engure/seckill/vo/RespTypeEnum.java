package com.engure.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RespTypeEnum {

    SUCCESS(200, "请求成功"),
    ERROR(500, "服务端异常");

    private final int code;
    private final String msg;

}
