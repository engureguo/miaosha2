package com.engure.seckill.vo;

import com.engure.seckill.validator.anno.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LoginVO {

    @NotNull
    @IsMobile(required = false) // 自定义校验规则
    private String mobile;

    @NotNull
    @Length(min = 32, max = 32, message = "密码错误") // 规定长度为 32
    private String password;
}
