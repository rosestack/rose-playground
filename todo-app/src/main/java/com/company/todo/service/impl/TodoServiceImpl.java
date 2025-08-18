package com.company.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.todo.domain.Todo;
import com.company.todo.mapper.TodoMapper;
import com.company.todo.service.TodoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoServiceImpl implements TodoService {
    private final TodoMapper todoMapper;

    public TodoServiceImpl(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Todo> pageTodo(Page<Todo> page, String title) {
        LambdaQueryWrapper<Todo> w = new LambdaQueryWrapper<>();
        w.like(title != null && !title.isBlank(), Todo::getTitle, title);
        return todoMapper.selectPage(page, w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Todo createTodo(Todo todo) {
        todoMapper.insert(todo);
        return todo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Todo updateTodo(Todo todo) {
        todoMapper.updateById(todo);
        return todoMapper.selectById(todo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        todoMapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Todo getById(Long id) {
        return todoMapper.selectById(id);
    }
}
