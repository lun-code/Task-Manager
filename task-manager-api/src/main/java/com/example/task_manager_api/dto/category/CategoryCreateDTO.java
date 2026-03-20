package com.example.task_manager_api.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateDTO(

        @NotBlank(message = "Category name cannot be blank")
        @Size(min = 3, max = 50, message = "Category name must contain between 3 and 50 characters")
        String name
) {}