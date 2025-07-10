package io.github.rose.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
