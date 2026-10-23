package com.team.task_manager.controller.error.exception.task;

public class InvalidTaskFilterException extends RuntimeException {
    public InvalidTaskFilterException(String message) {
        super(message);
    }
}
