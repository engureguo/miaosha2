package com.engure.seckill.validator.vo;

import com.engure.seckill.utils.ValidatorUtil;
import com.engure.seckill.validator.anno.IsMobile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 手机号校验规则
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    /**
     * 初始化，获取是否是必填
     *
     * @param constraintAnnotation
     */
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    /**
     * 校验 s 是否有效
     * @param s
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        if (required) {
            // 使用校验工具类进行校验
            return ValidatorUtil.isRightMobile(s);
        } else {
            // 不需要校验
            return true;
        }
    }
}
