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

    @io.swagger.v3.oas.annotations.Operation(summary = "分页查询 Todo 列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    })
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

    @io.swagger.v3.oas.annotations.Operation(summary = "创建 Todo")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "校验失败/重复标题"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "并发冲突")
    })
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

    @io.swagger.v3.oas.annotations.Operation(summary = "更新 Todo")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "校验失败/缺失或非法版本"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "并发冲突")
    })
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

    @io.swagger.v3.oas.annotations.Operation(summary = "获取 Todo 详情")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "不存在")
    })
    @GetMapping("/{id}")
    public ApiResponse<TodoResponse> get(@PathVariable Long id) {
        Todo t = todoService.getById(id);
        if (t == null) {
            throw new com.company.todo.exception.GlobalExceptionHandler.NotFoundException("not found");
        }
        return ApiResponse.ok(new TodoResponse(
                t.getId(), t.getTitle(), t.getDescription(), t.getCompleted(), t.getCreatedTime(), t.getUpdatedTime()));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "删除 Todo")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "不存在")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        todoService.deleteById(id);
        return ApiResponse.ok(null);
    }
}
