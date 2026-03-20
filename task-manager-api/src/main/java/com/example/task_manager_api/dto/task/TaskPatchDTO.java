package com.example.task_manager_api.dto.task;

import jakarta.validation.constraints.Size;

public record TaskPatchDTO(

        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
        String title,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        Boolean completed,

        Long categoryID
) {}