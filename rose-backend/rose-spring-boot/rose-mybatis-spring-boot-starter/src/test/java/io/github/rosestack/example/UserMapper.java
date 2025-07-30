package io.github.rosestack.example;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper 示例
 * <p>
 * 演示如何使用哈希字段进行查询
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据手机号哈希查询用户
     *
     * @param phoneHash 手机号哈希值
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE phone_hash = #{phoneHash}")
    UserEntity findByPhoneHash(@Param("phoneHash") String phoneHash);

    /**
     * 根据邮箱哈希查询用户
     *
     * @param emailHash 邮箱哈希值
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE email_hash = #{emailHash}")
    UserEntity findByEmailHash(@Param("emailHash") String emailHash);

    /**
     * 根据手机号或邮箱哈希查询用户
     *
     * @param phoneHash 手机号哈希值
     * @param emailHash 邮箱哈希值
     * @return 用户列表
     */
    @Select("SELECT * FROM user WHERE phone_hash = #{phoneHash} OR email_hash = #{emailHash}")
    List<UserEntity> findByPhoneHashOrEmailHash(@Param("phoneHash") String phoneHash, 
                                               @Param("emailHash") String emailHash);

    /**
     * 根据多个手机号哈希批量查询
     *
     * @param phoneHashes 手机号哈希值列表
     * @return 用户列表
     */
    @Select("<script>" +
            "SELECT * FROM user WHERE phone_hash IN " +
            "<foreach collection='phoneHashes' item='hash' open='(' separator=',' close=')'>" +
            "#{hash}" +
            "</foreach>" +
            "</script>")
    List<UserEntity> findByPhoneHashes(@Param("phoneHashes") List<String> phoneHashes);
}
