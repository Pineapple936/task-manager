package com.team.task_manager.controller.error.exception.task;

public class TaskCompletionBlockedException extends RuntimeException {

    public TaskCompletionBlockedException(Long taskId) {
        super("Task has incomplete dependencies: " + taskId);
    }
}
