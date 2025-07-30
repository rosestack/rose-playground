package com.company.usermodulith.user.internal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.usermodulith.user.UserCreateRequest;
import com.company.usermodulith.user.UserResponse;
import com.company.usermodulith.user.UserUpdateRequest;
import io.github.rosestack.core.model.PageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户对象转换器
 * <p>
 * 实现用户相关对象之间的转换，包括请求对象、实体对象、响应对象和分页对象的转换。
 * 放在 internal 包中隐藏实现细节，确保转换逻辑的封装性。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>手动处理对象字段映射，确保转换逻辑清晰</li>
 *   <li>支持空值忽略策略，避免覆盖现有数据</li>
 *   <li>自动忽略系统管理字段，确保数据安全</li>
 *   <li>支持分页对象转换，简化分页查询处理</li>
 * </ul>
 *
 * @author Chen Soul
 * @see UserEntity
 * @see UserCreateRequest
 * @see UserUpdateRequest
 * @see UserResponse
 * @since 1.0.0
 */
@Component
public class UserConverter {

    /**
     * 将用户创建请求转换为用户实体
     * <p>
     * 手动映射请求对象中的业务字段到实体对象，同时忽略系统管理字段。
     * 系统管理字段（如 ID、创建时间、更新时间等）由框架自动填充。
     * <p>
     * <strong>注意：</strong>密码字段会被直接映射，调用方需要确保密码已经过加密处理。
     *
     * @param request 用户创建请求，包含用户名、邮箱、手机号、密码等信息
     * @return 转换后的用户实体对象，不包含系统管理字段
     */
    public UserEntity toEntity(UserCreateRequest request) {
        if (request == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setUsername(request.getUsername());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setPassword(request.getPassword());
        // 系统管理字段由框架自动填充，这里不设置
        return entity;
    }

    /**
     * 将用户实体转换为用户响应对象
     * <p>
     * 手动映射实体对象中的字段到响应对象，排除敏感信息（如密码）。
     * UserResponse 中没有 password 字段，所以不会包含敏感信息。
     *
     * @param entity 用户实体对象，包含完整的用户信息
     * @return 用户响应对象，不包含敏感信息
     */
    public UserResponse toResponse(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setUsername(entity.getUsername());
        response.setEmail(entity.getEmail());
        response.setPhone(entity.getPhone());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setCreatedTime(entity.getCreatedTime());
        response.setUpdatedTime(entity.getUpdatedTime());
        // 不包含密码字段和审计字段
        return response;
    }

    /**
     * 将用户实体列表转换为用户响应列表
     * <p>
     * 批量转换用户实体对象为响应对象，常用于列表查询场景。
     *
     * @param entities 用户实体列表
     * @return 用户响应对象列表
     */
    public List<UserResponse> toListResponse(List<UserEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 使用更新请求更新用户实体
     * <p>
     * 将更新请求中的非空字段更新到现有的用户实体中，采用空值忽略策略。
     * 忽略系统管理字段和敏感字段（如密码），确保数据安全性。
     * <p>
     * <strong>注意：</strong>此方法会直接修改传入的实体对象。
     *
     * @param entity  要更新的用户实体对象
     * @param request 用户更新请求，包含要更新的字段
     */
    public void updateEntity(UserEntity entity, UserUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }

        // 只更新非空字段，忽略系统管理字段
        if (request.getUsername() != null) {
            entity.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone());
        }
        // 不更新密码、状态和系统管理字段
    }

    /**
     * 将 MyBatis Plus 分页对象转换为通用分页响应
     * <p>
     * 将 MyBatis Plus 的 IPage 分页结果转换为系统统一的 PageResponse 格式。
     * 自动处理分页元数据（当前页、页大小、总记录数）和数据记录的转换。
     * <p>
     * <strong>转换过程：</strong>
     * <ol>
     *   <li>提取分页元数据（页码、页大小、总数）</li>
     *   <li>将实体列表转换为响应对象列表</li>
     *   <li>构建统一的分页响应对象</li>
     * </ol>
     *
     * @param userPage MyBatis Plus 分页查询结果，包含用户实体列表和分页信息
     * @return 统一格式的分页响应对象，包含用户响应列表和分页元数据
     */
    public PageResponse<UserResponse> toPageResponse(IPage<UserEntity> userPage) {
        if (userPage == null) {
            return PageResponse.empty();
        }

        List<UserResponse> records = userPage.getRecords().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(records,
                userPage.getTotal(),
                userPage.getCurrent(),
                userPage.getSize()
        );
    }
}