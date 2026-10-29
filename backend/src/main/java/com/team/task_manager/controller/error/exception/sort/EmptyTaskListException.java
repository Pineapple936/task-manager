package com.team.task_manager.controller.error.exception.sort;

public class EmptyTaskListException extends RuntimeException {
    public EmptyTaskListException() {
        super("Task list cannot be empty");
    }
}