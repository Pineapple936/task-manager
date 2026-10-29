package com.team.task_manager.controller.error.exception.tag;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(Long tagId) {
        super("Tag with id %d was not found".formatted(tagId));
    }
}
