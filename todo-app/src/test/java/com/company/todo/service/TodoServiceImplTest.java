package com.company.todo.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.todo.domain.Todo;
import com.company.todo.exception.GlobalExceptionHandler;
import com.company.todo.mapper.TodoMapper;
import com.company.todo.service.impl.TodoServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TodoServiceImplTest {

    @Test
    void should_throw_conflict_when_optimistic_lock_failed() {
        TodoMapper mapper = Mockito.mock(TodoMapper.class);
        TodoServiceImpl svc = new TodoServiceImpl(mapper);
        Todo t = new Todo();
        t.setId(1L);
        when(mapper.selectById(1L)).thenReturn(t);
        when(mapper.updateById(any(Todo.class))).thenReturn(0);

        assertThatThrownBy(() -> svc.updateTodo(t)).isInstanceOf(GlobalExceptionHandler.OptimisticLockException.class);
    }

    @Test
    void should_throw_not_found_when_updating_missing() {
        TodoMapper mapper = Mockito.mock(TodoMapper.class);
        TodoServiceImpl svc = new TodoServiceImpl(mapper);
        Todo t = new Todo();
        t.setId(2L);
        when(mapper.selectById(2L)).thenReturn(null);

        assertThatThrownBy(() -> svc.updateTodo(t)).isInstanceOf(GlobalExceptionHandler.NotFoundException.class);
    }
}
