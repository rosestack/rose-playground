package com.example.ddddemo.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ddddemo.user.domain.entity.User;
import com.example.ddddemo.user.domain.repository.UserRepository;
import com.example.ddddemo.user.domain.valueobject.Address;
import com.example.ddddemo.user.infrastructure.converter.UserConverter;
import com.example.ddddemo.user.infrastructure.persistence.entity.UserDO;
import com.example.ddddemo.user.infrastructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户仓储实现类
 * <p>
 * 实现UserRepository接口，负责用户数据的持久化
 *
 * @author DDD Demo
 * @since 1.0.0
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    public UserRepositoryImpl(UserMapper userMapper, UserConverter userConverter) {
        this.userMapper = userMapper;
        this.userConverter = userConverter;
    }

    @Override
    public User save(User user) {
        UserDO userDO = toUserDO(user);
        
        if (userDO.getId() == null) {
            // 新增用户
            userMapper.insert(userDO);
            // 使用反射设置ID，因为setId是protected方法
            try {
                java.lang.reflect.Method setIdMethod = user.getClass().getDeclaredMethod("setId", Long.class);
                setIdMethod.setAccessible(true);
                setIdMethod.invoke(user, userDO.getId());
            } catch (Exception e) {
                throw new RuntimeException("设置用户ID失败", e);
            }
        } else {
            // 更新用户
            userMapper.updateById(userDO);
        }
        
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        UserDO userDO = userMapper.selectById(id);
        return Optional.ofNullable(userDO).map(this::toUser);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, username);
        UserDO userDO = userMapper.selectOne(wrapper);
        return Optional.ofNullable(userDO).map(this::toUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getEmail, email);
        UserDO userDO = userMapper.selectOne(wrapper);
        return Optional.ofNullable(userDO).map(this::toUser);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getPhone, phone);
        UserDO userDO = userMapper.selectOne(wrapper);
        return Optional.ofNullable(userDO).map(this::toUser);
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getEmail, email);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<User> findAll(int page, int size) {
        Page<UserDO> pageParam = new Page<>(page, size);
        Page<UserDO> result = userMapper.selectPage(pageParam, null);
        return result.getRecords().stream().map(this::toUser).collect(Collectors.toList());
    }

    @Override
    public List<User> findByStatus(Integer status, int page, int size) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getStatus, status);
        Page<UserDO> pageParam = new Page<>(page, size);
        Page<UserDO> result = userMapper.selectPage(pageParam, wrapper);
        return result.getRecords().stream().map(this::toUser).collect(Collectors.toList());
    }

    @Override
    public List<User> searchByKeyword(String keyword, int page, int size) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(UserDO::getUsername, keyword)
                .or()
                .like(UserDO::getEmail, keyword)
                .or()
                .like(UserDO::getRealName, keyword)
                .or()
                .like(UserDO::getNickname, keyword);
        Page<UserDO> pageParam = new Page<>(page, size);
        Page<UserDO> result = userMapper.selectPage(pageParam, wrapper);
        return result.getRecords().stream().map(this::toUser).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public long count() {
        return userMapper.selectCount(null);
    }

    @Override
    public long countByStatus(Integer status) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getStatus, status);
        return userMapper.selectCount(wrapper);
    }

    /**
     * 将用户领域对象转换为数据对象
     */
    private UserDO toUserDO(User user) {
        UserDO userDO = new UserDO();
        userDO.setId(user.getId());
        userDO.setUsername(user.getUsername());
        userDO.setEmail(user.getEmail());
        userDO.setPhone(user.getPhone());
        userDO.setPassword(user.getPassword());
        userDO.setRealName(user.getRealName());
        userDO.setNickname(user.getNickname());
        userDO.setAvatar(user.getAvatar());
        userDO.setGender(user.getGender());
        userDO.setBirthday(user.getBirthday());
        userDO.setStatus(user.getStatus());
        userDO.setLastLoginTime(user.getLastLoginTime());
        userDO.setCreateTime(user.getCreateTime());
        userDO.setUpdateTime(user.getUpdateTime());

        // 处理地址信息
        Address address = user.getAddress();
        if (address != null) {
            userDO.setCountry(address.getCountry());
            userDO.setProvince(address.getProvince());
            userDO.setCity(address.getCity());
            userDO.setDistrict(address.getDistrict());
            userDO.setDetailAddress(address.getDetailAddress());
            userDO.setPostalCode(address.getPostalCode());
        }

        return userDO;
    }

    /**
     * 将数据对象转换为用户领域对象
     */
    private User toUser(UserDO userDO) {
        // 创建地址值对象
        Address address = null;
        if (userDO.getCountry() != null || userDO.getProvince() != null || 
            userDO.getCity() != null || userDO.getDistrict() != null || 
            userDO.getDetailAddress() != null) {
            address = new Address(
                    userDO.getCountry(),
                    userDO.getProvince(),
                    userDO.getCity(),
                    userDO.getDistrict(),
                    userDO.getDetailAddress(),
                    userDO.getPostalCode()
            );
        }

        // 创建用户领域对象
        User user = User.create(
                userDO.getUsername(),
                userDO.getEmail(),
                userDO.getPhone(),
                userDO.getPassword(),
                userDO.getRealName()
        );

        user.setId(userDO.getId());

        user.updateBasicInfo(
                userDO.getRealName(),
                userDO.getNickname(),
                userDO.getAvatar(),
                userDO.getGender(),
                userDO.getBirthday(),
                address
        );
        user.updateStatus(userDO.getStatus());

        // 设置时间字段
        if (userDO.getLastLoginTime() != null) {
            user.updateLastLoginTime();
        }

        return user;
    }
} 