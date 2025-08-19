package com.company.todo.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.todo.domain.Todo;
import com.company.todo.exception.GlobalExceptionHandler;
import com.company.todo.service.TodoService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TodoController.class, properties = "spring.main.allow-bean-definition-overriding=true")
@Import(GlobalExceptionHandler.class)
class TodoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TodoService todoService;

    @Test
    @DisplayName("分页列表-空条件")
    void should_page_with_empty_condition() throws Exception {
        Page<Todo> page = new Page<>(1, 5, 0);
        Mockito.when(todoService.pageTodo(any(Page.class), isNull())).thenReturn(page);
        mockMvc.perform(get("/api/todos").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("创建-重复标题-返回400")
    void should_return_400_when_duplicate_title_on_create() throws Exception {
        Mockito.when(todoService.existsTitleExcludeId("t1", null)).thenReturn(true);
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("标题已存在")));
    }

    @Test
    @DisplayName("创建-空标题-返回400")
    void should_return_400_when_empty_title_on_create() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("更新-乐观锁冲突-返回409")
    void should_return_409_when_optimistic_lock_conflict_on_update() throws Exception {
        Mockito.when(todoService.updateTodo(Mockito.any()))
                .thenThrow(new GlobalExceptionHandler.OptimisticLockException("conflict"));
        mockMvc.perform(put("/api/todos/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t1\",\"description\":null,\"completed\":false,\"version\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("conflict"));
    }

    @Test
    @DisplayName("获取-不存在-返回404")
    void should_return_404_when_get_not_found() throws Exception {
        Mockito.when(todoService.getById(999L)).thenReturn(null);
        mockMvc.perform(get("/api/todos/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("删除-不存在-返回404")
    void should_return_404_when_delete_not_found() throws Exception {
        Mockito.doThrow(new GlobalExceptionHandler.NotFoundException("not found"))
                .when(todoService)
                .deleteById(999L);
        mockMvc.perform(delete("/api/todos/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("not found"));
    }

    @Test
    @DisplayName("创建-标题超长-返回400")
    void should_return_400_when_title_too_long_on_create() throws Exception {
        String longTitle = StringUtils.repeat('a', 101);
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + longTitle + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("创建-描述超长-返回400")
    void should_return_400_when_desc_too_long_on_create() throws Exception {
        String longDesc = StringUtils.repeat('b', 501);
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t1\",\"description\":\"" + longDesc + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
