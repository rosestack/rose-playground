package io.github.rose.user.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rose.user.dto.UserCreateRequest;
import io.github.rose.user.dto.UserResponse;
import io.github.rose.user.dto.UserUpdateRequest;
import io.github.rose.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 用户对象转换器
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserConverter {
    
    /**
     * 请求对象转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntity(UserCreateRequest request);
    
    /**
     * 实体转响应对象
     */
    UserResponse toResponse(User user);
    
    /**
     * 实体列表转响应列表
     */
    List<UserResponse> toResponseList(List<User> users);
    
    /**
     * 更新实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateRequest request);
    
    /**
     * 分页对象转换
     */
    default IPage<UserResponse> toPageResponse(IPage<User> userPage) {
        IPage<UserResponse> responsePage = new Page<>();
        responsePage.setCurrent(userPage.getCurrent());
        responsePage.setSize(userPage.getSize());
        responsePage.setTotal(userPage.getTotal());
        responsePage.setPages(userPage.getPages());
        
        List<UserResponse> records = userPage.getRecords().stream()
                .map(this::toResponse)
                .toList();
        responsePage.setRecords(records);
        
        return responsePage;
    }
}