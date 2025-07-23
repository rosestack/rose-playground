package io.github.rose.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rose.user.converter.UserConverter;
import io.github.rose.user.dto.UserCreateRequest;
import io.github.rose.user.dto.UserPageRequest;
import io.github.rose.user.dto.UserResponse;
import io.github.rose.user.dto.UserUpdateRequest;
import io.github.rose.user.entity.User;
import io.github.rose.user.exception.BusinessException;
import io.github.rose.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


/**
 * 用户服务实现类
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    @Override
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("查询用户，ID: {}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return userConverter.toResponse(user);
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("创建用户: {}", request.getUsername());

        // 业务校验
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        if (StringUtils.hasText(request.getEmail()) && userMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 创建用户
        User user = userConverter.toEntity(request);
        userMapper.insert(user);

        log.info("用户创建成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());
        return userConverter.toResponse(user);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("更新用户，ID: {}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新字段
        userConverter.updateEntity(user, request);
        userMapper.updateById(user);

        log.info("用户更新成功，用户ID：{}", user.getId());
        return userConverter.toResponse(user);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        log.info("删除用户，ID: {}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        userMapper.deleteById(id);
        log.info("用户删除成功，用户ID：{}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<UserResponse> pageUsers(Page<User> page, UserPageRequest request) {
        log.debug("分页查询用户，页码：{}，大小：{}", page.getCurrent(), page.getSize());

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getUsername()), User::getUsername, request.getUsername())
                .eq(request.getStatus() != null, User::getStatus, request.getStatus())
                .eq(User::getDeleted, false)
                .orderByDesc(User::getCreatedTime);

        // 执行分页查询
        IPage<User> userPage = userMapper.selectPage(page, wrapper);

        // 转换响应对象
        return userConverter.toPageResponse(userPage);
    }
}