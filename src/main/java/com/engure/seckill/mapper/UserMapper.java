package com.engure.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.engure.seckill.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author engure
 * @since 2021-08-10
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
