package com.example.task_manager_api.exception;

public class ResourceNotFoundException extends RuntimeException { // Para los 404 Not found
    public ResourceNotFoundException(String message) {
        super(message);
    }
}