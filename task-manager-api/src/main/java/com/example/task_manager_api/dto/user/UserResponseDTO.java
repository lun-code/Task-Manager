package com.example.task_manager_api.dto.user;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String fullName,
        String email,
        LocalDateTime createdAt
) {}