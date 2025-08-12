package io.github.rosestack.iam.infra.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.iam.domain.User;
import io.github.rosestack.iam.domain.UserRepository;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User>, UserRepository {
    default Optional<User> findByUsername(String username) {
        User user = selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return Optional.ofNullable(user);
    }

    default Optional<User> findByEmail(String email) {
        User user = selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        return Optional.ofNullable(user);
    }

    default Optional<User> findByPhone(String phone) {
        User user = selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        return Optional.ofNullable(user);
    }
}
