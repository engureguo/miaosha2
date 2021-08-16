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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author engure
 * @since 2021-08-10
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public RespBean doLogin(LoginVO vo, HttpServletRequest request, HttpServletResponse response) {

        // 合法性验证
        if (StringUtils.isEmpty(vo.getPassword()) ||
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
        request.getSession().setAttribute(ticket, user);

        return RespBean.success(RespTypeEnum.LOGIN_SUCCESS);
    }
}
