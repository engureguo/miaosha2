package com.engure.seckill.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class MD5Util {

    // 通用方法
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);//apache包下
    }

    /*************************************************************************************/

    //前端的盐（较为固定）
    private static final String salt = "!qwq**#$#-tk1$9&&7";

    /**
     * 第一次加密，密码框 到 提交的表单（前端）
     */
    public static String inputToForm(String in) {
        //混淆
        String complexStr = salt.charAt(3) + salt.charAt(8) +
                in + salt.charAt(0) + salt.charAt(9);
        return md5(complexStr);
    }

    /*************************************************************************************/

    /**
     * 后端，第二次加密，表单 到 数据库
     * @param formPass 表单密码，从请求中得到的密码，是前端加密后的结果
     * @param salt 【后端生成的随机盐】！！最终存放到数据库中
     * @return
     */
    public static String formToDb(String formPass, String salt) {
        String complexStr = salt.charAt(1) + salt.charAt(6) +
                formPass + salt.charAt(6) + salt.charAt(10);
        return md5(complexStr);
    }


}
