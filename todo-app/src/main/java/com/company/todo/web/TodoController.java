package com.company.todo.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.todo.domain.Todo;
import com.company.todo.service.TodoService;
import com.company.todo.web.dto.TodoDto.Create;
import com.company.todo.web.dto.TodoDto.Update;
import com.company.todo.web.dto.TodoResponse;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ApiResponse<Page<TodoResponse>> page(Page<Todo> pageable, @RequestParam(required = false) String keyword) {
        Page<Todo> page = todoService.pageTodo(pageable, keyword);
        Page<TodoResponse> mapped = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        mapped.setRecords(page.getRecords().stream()
                .map(t -> new TodoResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getCompleted(),
                        t.getCreatedTime(),
                        t.getUpdatedTime()))
                .collect(Collectors.toList()));
        return ApiResponse.ok(mapped);
    }

    @PostMapping
    public ApiResponse<TodoResponse> create(@RequestBody @Valid Create req) {
        Todo t = new Todo();
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setCompleted(false);
        Todo saved = todoService.createTodo(t);
        return ApiResponse.ok(new TodoResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getCompleted(),
                saved.getCreatedTime(),
                saved.getUpdatedTime()));
    }

    @PutMapping("/{id}")
    public ApiResponse<TodoResponse> update(@PathVariable Long id, @RequestBody @Valid Update req) {
        Todo t = new Todo();
        t.setId(id);
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setCompleted(req.completed());
        t.setVersion(req.version());
        Todo updated = todoService.updateTodo(t);
        return ApiResponse.ok(new TodoResponse(
                updated.getId(),
                updated.getTitle(),
                updated.getDescription(),
                updated.getCompleted(),
                updated.getCreatedTime(),
                updated.getUpdatedTime()));
    }

    @GetMapping("/{id}")
    public ApiResponse<TodoResponse> get(@PathVariable Long id) {
        Todo t = todoService.getById(id);
        return ApiResponse.ok(
                t == null
                        ? null
                        : new TodoResponse(
                                t.getId(),
                                t.getTitle(),
                                t.getDescription(),
                                t.getCompleted(),
                                t.getCreatedTime(),
                                t.getUpdatedTime()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        todoService.deleteById(id);
        return ApiResponse.ok(null);
    }
}
