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
        // 另外每次从 redis 中取 User 时，都要增长访问 user 的存活时间
        redisTemplate.opsForValue().set("user_ticket:" + ticket, user, 1, TimeUnit.HOURS);

        return RespBean.success(ticket);//返回ticket，后边要用
    }

    /**
     * 根据用户的 ticket，从 redis 中获取可能的登录信息。
     * </p>
     * 用户成功登陆后会将凭证写入 redis。
     * </p>
     * 注意：获取信息时，如果用户信息可以获取到，需要增加它的寿命
     *
     * @param request
     * @param response
     * @param ticket
     * @return
     */
    @Override
    public User getUserInfoByTicket(HttpServletRequest request, HttpServletResponse response, String ticket) {

        if (!StringUtils.hasLength(ticket))
            return null;

        User user = (User) redisTemplate.opsForValue().get("user_ticket:" + ticket);

        if (user != null) {
            redisTemplate.opsForValue().set("user_ticket:" + ticket, user, 1, TimeUnit.HOURS);
        }

        return user;
    }
}
