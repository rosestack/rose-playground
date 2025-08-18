package com.company.todo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.todo.domain.Todo;

public interface TodoService {
    Page<Todo> pageTodo(Page<Todo> page, String title);

    Todo createTodo(Todo todo);

    Todo updateTodo(Todo todo);

    void deleteById(Long id);

    Todo getById(Long id);

    boolean existsTitle(String value);
}
