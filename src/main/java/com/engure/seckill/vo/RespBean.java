package com.engure.seckill.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespBean {

    private Integer code;
    private String msg;
    private Object data;

    public static RespBean success() {
        return new RespBean(RespTypeEnum.SUCCESS.getCode(),
                RespTypeEnum.SUCCESS.getMsg(), null);
    }

    public static RespBean success(Object data) {
        return new RespBean(RespTypeEnum.SUCCESS.getCode(),
                RespTypeEnum.SUCCESS.getMsg(), data);
    }

    public static RespBean success(RespTypeEnum typeEnum) {
        return new RespBean(typeEnum.getCode(), typeEnum.getMsg(), null);
    }

    public static RespBean error(RespTypeEnum typeEnum) {
        return new RespBean(typeEnum.getCode(), typeEnum.getMsg(), null);
    }

    public static RespBean error(RespTypeEnum typeEnum, Object data) {
        return new RespBean(typeEnum.getCode(), typeEnum.getMsg(), data);
    }

    public static RespBean error(RespTypeEnum typeEnum, String msg, Object data) {
        return new RespBean(typeEnum.getCode(), msg, data);
    }


}
