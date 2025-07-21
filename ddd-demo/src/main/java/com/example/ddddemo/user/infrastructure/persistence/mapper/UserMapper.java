package com.example.ddddemo.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ddddemo.user.infrastructure.persistence.entity.UserDO;

/**
 * 用户Mapper接口
 * <p>
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public interface UserMapper extends BaseMapper<UserDO> {
} 