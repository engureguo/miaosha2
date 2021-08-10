package com.engure.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.engure.seckill.mapper.UserMapper;
import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author engure
 * @since 2021-08-10
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
