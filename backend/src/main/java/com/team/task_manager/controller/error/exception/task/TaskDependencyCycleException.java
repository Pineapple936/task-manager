package com.team.task_manager.controller.error.exception.task;

public class TaskDependencyCycleException extends RuntimeException {

    public TaskDependencyCycleException(Long taskId, Long dependencyTaskId) {
        super("Task dependency creates a cycle: " + taskId + " -> " + dependencyTaskId);
    }
}
