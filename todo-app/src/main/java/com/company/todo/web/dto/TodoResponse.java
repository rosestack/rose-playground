package com.company.todo.web.dto;

import java.time.LocalDateTime;

public record TodoResponse(
        Long id,
        String title,
        String description,
        Boolean completed,
        LocalDateTime createdTime,
        LocalDateTime updatedTime) {}
