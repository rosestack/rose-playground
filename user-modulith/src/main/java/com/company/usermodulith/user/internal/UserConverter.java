package com.company.usermodulith.user.internal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.usermodulith.user.UserCreateRequest;
import com.company.usermodulith.user.UserResponse;
import com.company.usermodulith.user.UserUpdateRequest;
import io.github.rosestack.core.model.PageResponse;
import org.mapstruct.*;

import java.util.List;

/**
 * 用户对象转换器
 * <p>
 * 使用 MapStruct 框架实现用户相关对象之间的转换，包括请求对象、实体对象、响应对象和分页对象的转换。
 * 放在 internal 包中隐藏实现细节，确保转换逻辑的封装性。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>自动处理对象字段映射，减少手动编码</li>
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
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserConverter {

    /**
     * 将用户创建请求转换为用户实体
     * <p>
     * 自动映射请求对象中的业务字段到实体对象，同时忽略系统管理字段。
     * 系统管理字段（如 ID、创建时间、更新时间等）由框架自动填充。
     * <p>
     * <strong>注意：</strong>密码字段会被直接映射，调用方需要确保密码已经过加密处理。
     *
     * @param request 用户创建请求，包含用户名、邮箱、手机号、密码等信息
     * @return 转换后的用户实体对象，不包含系统管理字段
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "createdTime", ignore = true),
            @Mapping(target = "updatedTime", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    UserEntity toEntity(UserCreateRequest request);

    /**
     * 将用户实体转换为用户响应对象
     * <p>
     * 自动映射实体对象中的字段到响应对象，排除敏感信息（如密码）。
     * UserResponse 中没有 password 字段，MapStruct 会自动忽略该字段。
     *
     * @param entity 用户实体对象，包含完整的用户信息
     * @return 用户响应对象，不包含敏感信息
     */
    UserResponse toResponse(UserEntity entity);

    /**
     * 将用户实体列表转换为用户响应列表
     * <p>
     * 批量转换用户实体对象为响应对象，常用于列表查询场景。
     *
     * @param entities 用户实体列表
     * @return 用户响应对象列表
     */
    List<UserResponse> toListResponse(List<UserEntity> entities);

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
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "createdTime", ignore = true),
            @Mapping(target = "updatedTime", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    void updateEntity(@MappingTarget UserEntity entity, UserUpdateRequest request);

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
    default PageResponse<UserResponse> toPageResponse(IPage<UserEntity> userPage) {
        List<UserResponse> records = userPage.getRecords().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(
                userPage.getCurrent(),
                userPage.getSize(),
                userPage.getTotal(),
                records
        );
    }
}