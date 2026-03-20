package com.example.task_manager_api.exception;

public class DataConflictException extends RuntimeException { // Para los 409 Conflict
    public DataConflictException(String message) {
        super(message);
    }
}
