package com.company.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.todo.domain.Todo;
import com.company.todo.exception.GlobalExceptionHandler;
import com.company.todo.mapper.TodoMapper;
import com.company.todo.service.impl.TodoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TodoServiceImplTest {

    private TodoMapper mapper;
    private TodoServiceImpl svc;

    @BeforeEach
    void setUp() {
        mapper = mock(TodoMapper.class);
        svc = new TodoServiceImpl(mapper);
    }

    @Test
    void should_throw_conflict_when_optimistic_lock_failed() {
        Todo t = new Todo();
        t.setId(1L);
        when(mapper.selectById(1L)).thenReturn(t);
        when(mapper.updateById(any(Todo.class))).thenReturn(0);

        assertThatThrownBy(() -> svc.updateTodo(t)).isInstanceOf(GlobalExceptionHandler.OptimisticLockException.class);
    }

    @Test
    void should_throw_not_found_when_updating_missing() {
        Todo t = new Todo();
        t.setId(2L);
        when(mapper.selectById(2L)).thenReturn(null);

        assertThatThrownBy(() -> svc.updateTodo(t)).isInstanceOf(GlobalExceptionHandler.NotFoundException.class);
    }

    @Test
    void should_update_success_and_return_fresh_entity() {
        Todo in = new Todo();
        in.setId(3L);
        Todo fresh = new Todo();
        fresh.setId(3L);
        when(mapper.selectById(3L)).thenReturn(new Todo());
        when(mapper.updateById(any(Todo.class))).thenReturn(1);
        when(mapper.selectById(3L)).thenReturn(fresh);

        Todo out = svc.updateTodo(in);
        assertThat(out).isSameAs(fresh);
    }

    @Test
    void should_create_and_return_same_entity() {
        Todo t = new Todo();
        t.setTitle("t1");
        when(mapper.insert(any(Todo.class))).thenReturn(1);

        Todo out = svc.createTodo(t);
        assertThat(out).isSameAs(t);
        verify(mapper).insert(t);
    }

    @Test
    void should_page_like_when_title_present() {
        Page<Todo> page = new Page<>(1, 5);
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<Todo> out = svc.pageTodo(page, "milk");
        assertThat(out).isSameAs(page);
        verify(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void should_page_without_condition_when_title_blank() {
        Page<Todo> page = new Page<>(1, 5);
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<Todo> out = svc.pageTodo(page, "  ");
        assertThat(out).isSameAs(page);
    }

    @Test
    void should_delegate_get_delete_exists() {
        Todo t = new Todo();
        when(mapper.selectById(10L)).thenReturn(t);
        assertThat(svc.getById(10L)).isSameAs(t);
        verify(mapper).selectById(10L);

        svc.deleteById(11L);
        verify(mapper).deleteById(11L);

        when(mapper.existsTitleExcludeId("x", 1L)).thenReturn(true);
        assertThat(svc.existsTitleExcludeId("x", 1L)).isTrue();
        verify(mapper).existsTitleExcludeId("x", 1L);
    }
}
