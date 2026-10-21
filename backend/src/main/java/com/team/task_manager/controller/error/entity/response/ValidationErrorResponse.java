package com.team.task_manager.controller.error.entity.response;

import java.util.Map;

public record ValidationErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
}
