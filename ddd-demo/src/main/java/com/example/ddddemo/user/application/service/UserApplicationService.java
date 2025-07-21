package com.example.ddddemo.user.application.service;

import com.example.ddddemo.shared.application.dto.ApiResponse;
import com.example.ddddemo.user.application.command.CreateUserCommand;
import com.example.ddddemo.user.application.command.UpdateUserCommand;
import com.example.ddddemo.user.application.command.UpdateUserStatusCommand;
import com.example.ddddemo.user.application.query.UserQuery;
import com.example.ddddemo.user.application.dto.UserDTO;
import com.example.ddddemo.user.domain.entity.User;
import com.example.ddddemo.user.domain.repository.UserRepository;
import com.example.ddddemo.user.domain.valueobject.Address;
import com.example.ddddemo.user.infrastructure.converter.UserConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户应用服务
 * <p>
 * 负责用户相关的业务用例，协调领域对象完成业务操作
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>用户创建、更新、查询业务用例</li>
 *   <li>业务规则验证</li>
 *   <li>DTO转换</li>
 * </ul>
 *
 * @author DDD Demo
 * @since 1.0.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    public UserApplicationService(UserRepository userRepository, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    /**
     * 创建用户
     *
     * @param command 创建用户命令
     * @return 创建结果
     */
    public ApiResponse<UserDTO> createUser(CreateUserCommand command) {
        // 业务规则验证
        if (userRepository.existsByUsername(command.getUsername())) {
            return ApiResponse.error("USERNAME_EXISTS", "用户名已存在");
        }
        if (userRepository.existsByEmail(command.getEmail())) {
            return ApiResponse.error("EMAIL_EXISTS", "邮箱已存在");
        }
        if (command.getPhone() != null && userRepository.existsByPhone(command.getPhone())) {
            return ApiResponse.error("PHONE_EXISTS", "手机号已存在");
        }

        // 创建用户领域对象
        User user = User.create(
                command.getUsername(),
                command.getEmail(),
                command.getPhone(),
                command.getPassword(),
                command.getRealName()
        );

        // 保存用户
        User savedUser = userRepository.save(user);

        // 转换为DTO并返回
        UserDTO userDTO = userConverter.toDTO(savedUser);
        return ApiResponse.success(userDTO);
    }

    /**
     * 更新用户基本信息
     *
     * @param userId 用户ID
     * @param command 更新用户命令
     * @return 更新结果
     */
    public ApiResponse<UserDTO> updateUser(Long userId, UpdateUserCommand command) {
        // 查找用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("USER_NOT_FOUND", "用户不存在");
        }

        User user = userOpt.get();

        // 创建地址值对象
        Address address = null;
        if (command.getAddress() != null) {
            address = new Address(
                    command.getAddress().getCountry(),
                    command.getAddress().getProvince(),
                    command.getAddress().getCity(),
                    command.getAddress().getDistrict(),
                    command.getAddress().getDetailAddress(),
                    command.getAddress().getPostalCode()
            );
        }

        // 更新用户基本信息
        user.updateBasicInfo(
                command.getRealName(),
                command.getNickname(),
                command.getAvatar(),
                command.getGender(),
                command.getBirthday(),
                address
        );

        // 保存用户
        User savedUser = userRepository.save(user);

        // 转换为DTO并返回
        UserDTO userDTO = userConverter.toDTO(savedUser);
        return ApiResponse.success(userDTO);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param command 更新状态命令
     * @return 更新结果
     */
    public ApiResponse<UserDTO> updateUserStatus(Long userId, UpdateUserStatusCommand command) {
        // 查找用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("USER_NOT_FOUND", "用户不存在");
        }

        User user = userOpt.get();

        // 更新用户状态
        user.updateStatus(command.getStatus());

        // 保存用户
        User savedUser = userRepository.save(user);

        // 转换为DTO并返回
        UserDTO userDTO = userConverter.toDTO(savedUser);
        return ApiResponse.success(userDTO);
    }

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public ApiResponse<UserDTO> getUserById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("USER_NOT_FOUND", "用户不存在");
        }

        UserDTO userDTO = userConverter.toDTO(userOpt.get());
        return ApiResponse.success(userDTO);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    public ApiResponse<UserDTO> getUserByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("USER_NOT_FOUND", "用户不存在");
        }

        UserDTO userDTO = userConverter.toDTO(userOpt.get());
        return ApiResponse.success(userDTO);
    }

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 用户列表
     */
    public ApiResponse<List<UserDTO>> getUserList(UserQuery query) {
        List<User> users;
        
        if (query.getStatus() != null) {
            // 根据状态查询
            users = userRepository.findByStatus(query.getStatus(), query.getPage(), query.getSize());
        } else if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            // 根据关键词搜索
            users = userRepository.searchByKeyword(query.getKeyword(), query.getPage(), query.getSize());
        } else {
            // 查询所有用户
            users = userRepository.findAll(query.getPage(), query.getSize());
        }

        List<UserDTO> userDTOs = users.stream()
                .map(userConverter::toDTO)
                .collect(Collectors.toList());

        return ApiResponse.success(userDTOs);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    public ApiResponse<Void> deleteUser(Long userId) {
        // 查找用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("USER_NOT_FOUND", "用户不存在");
        }

        // 删除用户
        userRepository.deleteById(userId);
        return ApiResponse.success();
    }

    /**
     * 统计用户数量
     *
     * @param status 用户状态（可选）
     * @return 用户数量
     */
    public ApiResponse<Long> countUsers(Integer status) {
        long count;
        if (status != null) {
            count = userRepository.countByStatus(status);
        } else {
            count = userRepository.count();
        }
        return ApiResponse.success(count);
    }
} 