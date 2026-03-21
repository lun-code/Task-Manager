package com.example.task_manager_api.dto.task;

import java.util.List;

public record TaskPageResponseDTO(
        List<TaskResponseDTO> tasks, // Las tareas de esta página
        int currentPage, // En qué página estamos (empieza en 0)
        int totalPages, // Cuántas páginas hay en total
        long totalElements // Cuántas tareas hay en total
) {}