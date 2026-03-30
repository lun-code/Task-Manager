package com.example.task_manager_api.dto.user;

import lombok.Builder;

@Builder
public record LoginResponse(
        String token,
        Long expiresIn
) {
}
