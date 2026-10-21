package com.team.task_manager.controller.error.entity.response;

public record ErrorResponse(
        int status,
        String error,
        String message
) {
}
