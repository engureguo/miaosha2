package com.engure.seckill.exception;

import com.engure.seckill.vo.RespTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局异常
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalException extends RuntimeException {
    private RespTypeEnum respTypeEnum;
}
