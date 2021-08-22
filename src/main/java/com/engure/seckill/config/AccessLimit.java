package com.engure.seckill.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {

    //次数
    int count();

    //多长时间内
    int seconds();

    //是否需要登录
    boolean needLogin() default true;
}
