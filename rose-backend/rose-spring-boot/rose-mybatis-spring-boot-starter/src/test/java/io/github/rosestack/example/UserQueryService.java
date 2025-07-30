package io.github.rosestack.example;

import io.github.rosestack.mybatis.support.encryption.hash.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户查询服务示例
 * <p>
 * 演示如何使用哈希查询功能
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserMapper userMapper;
    private final HashService hashService;

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号明文
     * @return 用户信息
     */
    public UserEntity findByPhone(String phone) {
        // 根据 UserEntity.phone 字段的注解获取正确的哈希算法
        String phoneHash = hashService.generateHashByField(phone, UserEntity.class, "phone");
        UserEntity user = userMapper.findByPhoneHash(phoneHash);

        if (user != null) {
            log.info("根据手机号查询到用户: {}", user.getUsername());
        } else {
            log.info("未找到手机号为 {} 的用户", phone);
        }

        return user;
    }

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱明文
     * @return 用户信息
     */
    public UserEntity findByEmail(String email) {
        // 根据 UserEntity.email 字段的注解获取正确的哈希算法（HMAC_SHA256）
        String emailHash = hashService.generateHashByField(email, UserEntity.class, "email");
        UserEntity user = userMapper.findByEmailHash(emailHash);
        
        if (user != null) {
            log.info("根据邮箱查询到用户: {}", user.getUsername());
        } else {
            log.info("未找到邮箱为 {} 的用户", email);
        }
        
        return user;
    }

    /**
     * 根据手机号或邮箱查询用户（用于登录）
     *
     * @param phoneOrEmail 手机号或邮箱明文
     * @return 用户列表
     */
    public List<UserEntity> findByPhoneOrEmail(String phoneOrEmail) {
        // 分别根据字段注解获取正确的哈希算法
        String phoneHash = hashService.generateHashByField(phoneOrEmail, UserEntity.class, "phone");
        String emailHash = hashService.generateHashByField(phoneOrEmail, UserEntity.class, "email");
        
        List<UserEntity> users = userMapper.findByPhoneHashOrEmailHash(phoneHash, emailHash);
        log.info("根据手机号或邮箱查询到 {} 个用户", users.size());
        
        return users;
    }

    /**
     * 批量查询用户（根据手机号列表）
     *
     * @param phones 手机号明文列表
     * @return 用户列表
     */
    public List<UserEntity> findByPhones(List<String> phones) {
        List<String> phoneHashes = phones.stream()
                .map(phone -> hashService.generateHashByField(phone, UserEntity.class, "phone"))
                .collect(Collectors.toList());

        List<UserEntity> users = userMapper.findByPhoneHashes(phoneHashes);
        log.info("批量查询 {} 个手机号，找到 {} 个用户", phones.size(), users.size());

        return users;
    }

    /**
     * 验证手机号是否匹配（防时序攻击）
     *
     * @param inputPhone    输入的手机号
     * @param storedPhoneHash 存储的手机号哈希
     * @return 是否匹配
     */
    public boolean verifyPhone(String inputPhone, String storedPhoneHash) {
        // 根据 UserEntity.phone 字段的注解获取正确的哈希算法
        String computedHash = hashService.generateHashByField(inputPhone, UserEntity.class, "phone");
        return hashService.constantTimeEquals(computedHash, storedPhoneHash);
    }

    /**
     * 验证邮箱是否匹配（防时序攻击）
     *
     * @param inputEmail    输入的邮箱
     * @param storedEmailHash 存储的邮箱哈希
     * @return 是否匹配
     */
    public boolean verifyEmail(String inputEmail, String storedEmailHash) {
        // 根据 UserEntity.email 字段的注解获取正确的哈希算法
        String computedHash = hashService.generateHashByField(inputEmail, UserEntity.class, "email");
        return hashService.constantTimeEquals(computedHash, storedEmailHash);
    }

    /**
     * 保存用户（演示自动加密和哈希生成）
     *
     * @param user 用户信息
     * @return 保存后的用户信息
     */
    public UserEntity saveUser(UserEntity user) {
        // 插入时会自动：
        // 1. 加密 phone、email、idCard、bankCard 字段
        // 2. 生成 phoneHash、emailHash 字段
        // 3. 填充 createdTime、updatedTime 等字段
        int result = userMapper.insert(user);
        
        if (result > 0) {
            log.info("用户保存成功: {}", user.getUsername());
        } else {
            log.error("用户保存失败");
        }
        
        return user;
    }
}
