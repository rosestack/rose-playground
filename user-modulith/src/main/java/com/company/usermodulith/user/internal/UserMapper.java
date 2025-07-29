package com.company.usermodulith.user.internal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.mybatis.annotation.DataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    UserEntity selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体，如果不存在返回 null
     */
    @Select("SELECT * FROM user WHERE email = #{email} AND deleted = 0")
    UserEntity selectByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM user WHERE username = #{username} AND deleted = 0")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM user WHERE email = #{email} AND deleted = 0")
    boolean existsByEmail(@Param("email") String email);
} 