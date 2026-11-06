package com.team.task_manager.controller.error.exception.task;

public class TaskMemberAlreadyExistsException extends RuntimeException {

    public TaskMemberAlreadyExistsException(Long taskId, Long userId) {
        super("User is already a task member: taskId=" + taskId + ", userId=" + userId);
    }
}
