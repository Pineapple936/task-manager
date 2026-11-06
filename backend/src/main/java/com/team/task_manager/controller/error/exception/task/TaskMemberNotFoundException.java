package com.team.task_manager.controller.error.exception.task;

public class TaskMemberNotFoundException extends RuntimeException {

    public TaskMemberNotFoundException(Long taskId, Long userId) {
        super("Task member is not found: taskId=" + taskId + ", userId=" + userId);
    }
}
