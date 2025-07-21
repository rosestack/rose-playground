package com.example.ddddemo.user.domain.repository;

import com.example.ddddemo.user.domain.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 * <p>
 * 定义用户聚合根的数据访问方法
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>用户数据持久化</li>
 *   <li>用户数据查询</li>
 *   <li>业务规则验证</li>
 * </ul>
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public interface UserRepository {

    /**
     * 保存用户
     *
     * @param user 用户实体
     * @return 保存后的用户
     */
    User save(User user);

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户实体
     */
    Optional<User> findById(Long id);

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 分页查询用户列表
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> findAll(int page, int size);

    /**
     * 根据状态查询用户列表
     *
     * @param status 用户状态
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> findByStatus(Integer status, int page, int size);

    /**
     * 根据关键词搜索用户
     *
     * @param keyword 搜索关键词（用户名、邮箱、真实姓名）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> searchByKeyword(String keyword, int page, int size);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 统计用户总数
     *
     * @return 用户总数
     */
    long count();

    /**
     * 根据状态统计用户数量
     *
     * @param status 用户状态
     * @return 用户数量
     */
    long countByStatus(Integer status);
} 