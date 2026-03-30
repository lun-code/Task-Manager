package com.example.task_manager_api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginUserDto(

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password
) {}