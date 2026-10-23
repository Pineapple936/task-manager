package com.team.task_manager.controller.error.exception.task;

public class TaskDependencyAlreadyExistsException extends RuntimeException {

    public TaskDependencyAlreadyExistsException(Long taskId, Long dependencyTaskId) {
        super("Task dependency already exists: " + taskId + " -> " + dependencyTaskId);
    }
}
