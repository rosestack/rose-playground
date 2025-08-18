package com.company.todo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TodoDto {
    public record Create(@NotBlank @Size(max = 100) String title, @Size(max = 500) String description) {}

    public record Update(
            @NotBlank Long id,
            @NotBlank @Size(max = 100) String title,
            @Size(max = 500) String description,
            Boolean completed,
            Integer version) {}
}
