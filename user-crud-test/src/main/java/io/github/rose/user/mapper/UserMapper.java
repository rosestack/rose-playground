package io.github.rose.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);
    
    /**
     * 检查用户名是否存在
     */
    default boolean existsByUsername(String username) {
        return selectByUsername(username) != null;
    }
    
    /**
     * 检查邮箱是否存在
     */
    default boolean existsByEmail(String email) {
        return selectByEmail(email) != null;
    }
}