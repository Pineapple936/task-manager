package com.team.task_manager.controller.error.exception.tag;

public class TaskTagNotFoundException extends RuntimeException {
    public TaskTagNotFoundException(Long taskId, Long tagId) {
        super("Tag %d is not assigned to task %d".formatted(tagId, taskId));
    }
}
