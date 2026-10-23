package com.team.task_manager.controller.error.exception.task;

public class InvalidTaskFilterException extends RuntimeException {
    public InvalidTaskFilterException() {
        super("The start date cannot be later than the end date");
    }
}
