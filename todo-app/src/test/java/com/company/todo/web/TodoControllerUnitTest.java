package com.company.todo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.todo.domain.Todo;
import com.company.todo.service.TodoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TodoController.class, properties = "spring.main.allow-bean-definition-overriding=true")
@org.springframework.context.annotation.Import(com.company.todo.exception.GlobalExceptionHandler.class)
class TodoControllerUnitTest {

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
}
