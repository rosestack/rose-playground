package com.company.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.todo.domain.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {}
