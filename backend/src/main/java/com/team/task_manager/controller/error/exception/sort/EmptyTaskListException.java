package com.team.task_manager.controller.error.exception.sort;

public class EmptyTaskListException extends RuntimeException {
    public EmptyTaskListException(String message) {
        super(message);
    }
}