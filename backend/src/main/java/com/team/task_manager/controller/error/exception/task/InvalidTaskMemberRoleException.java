package com.team.task_manager.controller.error.exception.task;

public class InvalidTaskMemberRoleException extends RuntimeException {

    public InvalidTaskMemberRoleException() {
        super("Only editor and viewer roles can be assigned");
    }
}
