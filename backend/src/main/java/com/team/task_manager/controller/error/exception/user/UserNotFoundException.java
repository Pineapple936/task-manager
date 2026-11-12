package com.team.task_manager.controller.error.exception.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("User is not found: " + username);
    }

    public UserNotFoundException(Long userId) {
        super("User is not found: " + userId);
    }
}
