package com.engure.seckill.config;

import com.engure.seckill.pojo.User;

/**
 * 存放用户信息，在 AccessLimitInterceptor 中预处理
 */
public class UserContext {

    private static final ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public static void set(User user) {
        threadLocal.set(user);
    }

    public static User get() {
        return threadLocal.get();
    }

}
