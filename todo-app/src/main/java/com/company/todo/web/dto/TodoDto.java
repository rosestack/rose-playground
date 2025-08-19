package com.company.todo.web.dto;

import com.company.todo.web.validation.TitleUnique;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "TodoDto")
public class TodoDto {
    @Schema(name = "Create")
    public record Create(
            @Schema(description = "标题", example = "Buy milk") @NotBlank @Size(max = 100) @TitleUnique String title,
            @Schema(description = "描述", example = "2L milk for tomorrow") @Size(max = 500) String description) {}

    @Schema(name = "Update")
    public record Update(
            @Schema(description = "标题", example = "Buy milk") @NotBlank @Size(max = 100) @TitleUnique String title,
            @Schema(description = "描述", example = "2L milk for tomorrow") @Size(max = 500) String description,
            @Schema(description = "是否完成", example = "false") Boolean completed,
            @Schema(description = "版本号", example = "1") @NotNull Integer version) {}
}
