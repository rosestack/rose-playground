package com.company.usermodulith.user.internal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.usermodulith.user.*;
import io.github.rosestack.iam.*;
import com.company.usermodulith.user.event.UserCreatedEvent;
import com.company.usermodulith.user.event.UserUpdatedEvent;
import io.github.rosestack.core.model.PageRequest;
import io.github.rosestack.core.model.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现类
 * <p>
 * 放在 internal 包中隐藏实现细节
 * 实现用户模块的业务逻辑
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserConverter userConverter;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse createUser(UserCreateRequest request) {
        // 业务校验
        if (userMapper.existsByUsername(request.getUsername())) {
            throw UserException.usernameAlreadyExists(request.getUsername());
        }
        if (userMapper.existsByEmail(request.getEmail())) {
            throw UserException.emailAlreadyExists(request.getEmail());
        }

        // 创建用户实体
        UserEntity user = userConverter.toEntity(request);
        user.setStatus(UserStatus.ACTIVE);
        userMapper.insert(user);

        // 发布用户创建事件
        UserCreatedEvent event = new UserCreatedEvent(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedTime()
        );
        eventPublisher.publishEvent(event);

        return userConverter.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw UserException.userNotFound(id);
        }
        return userConverter.toResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw UserException.userNotFound(id);
        }

        // 更新字段
        userConverter.updateEntity(user, request);
        userMapper.updateById(user);

        // 发布用户更新事件
        UserUpdatedEvent event = new UserUpdatedEvent(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUpdatedTime()
        );
        eventPublisher.publishEvent(event);

        return userConverter.toResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw UserException.userNotFound(id);
        }
        userMapper.deleteById(id);
    }

    @Override
    public PageResponse<UserResponse> pageUsers(PageRequest pageRequest, UserQuery userQuery) {
        Page<UserEntity> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());

        // 构建查询条件
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(userQuery.getUsername()), UserEntity::getUsername, userQuery.getUsername())
                .like(StringUtils.hasText(userQuery.getEmail()), UserEntity::getEmail, userQuery.getEmail())
                .eq(StringUtils.hasText(userQuery.getStatus()), UserEntity::getStatus, userQuery.getStatus())
                .orderByDesc(UserEntity::getCreatedTime);

        IPage<UserEntity> userPage = userMapper.selectPage(page, wrapper);

        return userConverter.toPageResponse(userPage);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserEntity::getCreatedTime);

        List<UserEntity> users = userMapper.selectList(wrapper);
        return userConverter.toListResponse(users);
    }
}