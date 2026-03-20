package com.example.task_manager_api.dto.category;

import lombok.Builder;

@Builder
public record CategoryResponseDTO(
        Long id,
        String name
) {}