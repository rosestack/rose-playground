package com.example.ddddemo.user.infrastructure.converter;

import com.example.ddddemo.user.application.dto.AddressDTO;
import com.example.ddddemo.user.application.dto.UserDTO;
import com.example.ddddemo.user.domain.entity.User;
import com.example.ddddemo.user.domain.valueobject.Address;
import org.springframework.stereotype.Component;

/**
 * 用户转换器
 * <p>
 * 负责用户领域对象和DTO之间的转换
 *
 * @author DDD Demo
 * @since 1.0.0
 */
@Component
public class UserConverter {

    /**
     * 将用户领域对象转换为DTO
     *
     * @param user 用户领域对象
     * @return 用户DTO
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        AddressDTO addressDTO = null;
        if (user.getAddress() != null) {
            addressDTO = new AddressDTO(
                    user.getAddress().getCountry(),
                    user.getAddress().getProvince(),
                    user.getAddress().getCity(),
                    user.getAddress().getDistrict(),
                    user.getAddress().getDetailAddress(),
                    user.getAddress().getPostalCode()
            );
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRealName(),
                user.getNickname(),
                user.getAvatar(),
                user.getGender(),
                user.getBirthday(),
                addressDTO,
                user.getStatus(),
                user.getLastLoginTime(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    /**
     * 将DTO转换为用户领域对象
     *
     * @param userDTO 用户DTO
     * @return 用户领域对象
     */
    public User toEntity(UserDTO userDTO) {
        // 由于User实体使用了私有构造函数，这里暂时不实现
        // 实际项目中可以通过工厂模式或其他方式实现
        throw new UnsupportedOperationException("DTO to Entity conversion not implemented");
    }
} 