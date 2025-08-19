package com.company.todo.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "TodoResponse")
public record TodoResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "标题", example = "Buy milk") String title,
        @Schema(description = "描述", example = "2L milk for tomorrow") String description,
        @Schema(description = "是否完成", example = "false") Boolean completed,
        @Schema(description = "创建时间") LocalDateTime createdTime,
        @Schema(description = "更新时间") LocalDateTime updatedTime) {}
