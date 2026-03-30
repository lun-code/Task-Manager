package com.example.task_manager_api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDto(

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Full name cannot be blank")
        @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
        String fullName
) {}