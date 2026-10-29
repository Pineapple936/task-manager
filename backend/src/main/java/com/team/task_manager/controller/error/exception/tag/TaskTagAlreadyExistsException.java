package com.team.task_manager.controller.error.exception.tag;

public class TaskTagAlreadyExistsException extends RuntimeException {
    public TaskTagAlreadyExistsException(Long taskId, Long tagId) {
        super("Tag %d is already assigned to task %d".formatted(tagId, taskId));
    }
}
