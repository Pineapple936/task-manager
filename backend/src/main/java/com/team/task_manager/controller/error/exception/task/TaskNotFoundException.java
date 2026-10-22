package com.team.task_manager.controller.error.exception.task;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long taskId) {
        super("Task is not found: " + taskId);
    }
}
