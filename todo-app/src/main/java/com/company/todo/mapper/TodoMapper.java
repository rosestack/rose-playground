package com.company.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.todo.domain.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {
    default boolean existsTitleExcludeId(String title, Long id) {
        return exists(Wrappers.<Todo>lambdaQuery().eq(Todo::getTitle, title).ne(id != null, Todo::getId, id));
    }
}
