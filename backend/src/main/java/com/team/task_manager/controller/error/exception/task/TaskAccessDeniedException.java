package com.team.task_manager.controller.error.exception.task;

public class TaskAccessDeniedException extends RuntimeException {

    public TaskAccessDeniedException(Long taskId) {
        super("Access to task is denied: " + taskId);
    }
}
