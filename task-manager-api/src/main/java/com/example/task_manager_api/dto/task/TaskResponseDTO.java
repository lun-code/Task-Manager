package com.example.task_manager_api.dto.task;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt,
        String categoryName
) {}