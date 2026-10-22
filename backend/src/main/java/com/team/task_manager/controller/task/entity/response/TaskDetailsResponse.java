package com.team.task_manager.controller.task.entity.response;

import java.time.OffsetDateTime;

import com.team.task_manager.controller.task.entity.TaskApiStatus;

public record TaskDetailsResponse(
        Long id,
        String name,
        Integer priority,
        TaskApiStatus status,
        OffsetDateTime deadline,
        OffsetDateTime createdAt,
        String description
) {
}
