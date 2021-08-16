package com.engure.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.engure.seckill.exception.GlobalException;
import com.engure.seckill.mapper.UserMapper;
import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IUserService;
import com.engure.seckill.utils.CookieUtil;
import com.engure.seckill.utils.MD5Util;
import com.engure.seckill.utils.UUIDUtil;
import com.engure.seckill.utils.ValidatorUtil;
import com.engure.seckill.vo.LoginVO;
import com.engure.seckill.vo.RespBean;
import com.engure.seckill.vo.RespTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author engure
 * @since 2021-08-10
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RespBean doLogin(LoginVO vo, HttpServletRequest request, HttpServletResponse response) {

        // 合法性验证
        if (!StringUtils.hasLength(vo.getPassword()) ||
                !ValidatorUtil.isRightMobile(vo.getMobile()))
            throw new GlobalException(RespTypeEnum.LOGIN_ERROR);

        //根据手机号查找用户
        User user = userMapper.selectById(vo.getMobile());
        if (user == null)
            throw new GlobalException(RespTypeEnum.USER_NOT_REGISTERED);

        // 第二次md5(formPassword, 用户的盐) ?  dbPassword
        if (!MD5Util.formToDb(vo.getPassword(), user.getSalt()).equals(user.getPassword()))
            throw new GlobalException(RespTypeEnum.LOGIN_ERROR);

        // cookie+session思路。cookie-token <-> token-userInfo
        String ticket = UUIDUtil.uuid();
        CookieUtil.setCookie(request, response, "user_ticket", ticket);
        //request.getSession().setAttribute(ticket, user);
        // 存入redis
        redisTemplate.opsForValue().set("user_ticket:" + ticket, user, 1, TimeUnit.HOURS);

        return RespBean.success(RespTypeEnum.LOGIN_SUCCESS);
    }

    @Override
    public User getUserInfoByTicket(String ticket) {

        if (!StringUtils.hasLength(ticket))
            return null;

        return (User) redisTemplate.opsForValue().get("user_ticket:" + ticket);
    }
}
