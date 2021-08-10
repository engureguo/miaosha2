package com.engure.seckill.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author engure
 * @since 2021-08-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id，手机号码
     */
    private Long id;

    private String nickname;

    /**
     * md5( md5(pass明文+固定salt) + salt )
     */
    private String password;

    private String salt;

    /**
     * 头像
     */
    private String avator;

    /**
     * 注册时间
     */
    private Date registerDate;

    /**
     * 最后一次登录时间
     */
    private Date lastLoginDate;

    /**
     * 登陆次数
     */
    private Integer loginCount;


}
