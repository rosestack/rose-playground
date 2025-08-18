package com.company.usermodulith.user.internal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.mybatis.annotation.DataPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口
 * <p>
 * 放在 internal 包中隐藏实现细节
 * 继承 MyBatis Plus 的 BaseMapper
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Mapper
@DataPermission(field = "id", fieldType = DataPermission.FieldType.NUMBER)
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，如果不存在返回 null
     */
    default UserEntity selectByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username)
               .last("LIMIT 1");
        return selectOne(wrapper);
    }

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体，如果不存在返回 null
     */
    default UserEntity selectByEmail(String email) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email)
               .last("LIMIT 1");
        return selectOne(wrapper);
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    default boolean existsByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username)
               .last("LIMIT 1");
        return selectCount(wrapper) > 0;
    }

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    default boolean existsByEmail(String email) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email)
               .last("LIMIT 1");
        return selectCount(wrapper) > 0;
    }
} 