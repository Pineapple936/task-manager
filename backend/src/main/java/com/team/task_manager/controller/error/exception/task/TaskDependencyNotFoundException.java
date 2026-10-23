package com.team.task_manager.controller.error.exception.task;

public class TaskDependencyNotFoundException extends RuntimeException {

    public TaskDependencyNotFoundException(Long taskId, Long dependencyTaskId) {
        super("Task dependency not found: " + taskId + " -> " + dependencyTaskId);
    }
}
