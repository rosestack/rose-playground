package com.company.todo.web.dto;

import com.company.todo.service.TitleUnique;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TodoDto {
    public record Create(@NotBlank @Size(max = 100) @TitleUnique String title, @Size(max = 500) String description) {}

    public record Update(
            @NotBlank @Size(max = 100) @TitleUnique String title,
            @Size(max = 500) String description,
            Boolean completed,
            @NotNull Integer version) {}
}
