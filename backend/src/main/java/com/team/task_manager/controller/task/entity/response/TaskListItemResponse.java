package com.team.task_manager.controller.task.entity.response;

import java.time.OffsetDateTime;
import java.util.List;

import com.team.task_manager.controller.task.entity.TaskApiStatus;

public record TaskListItemResponse(
        Long id,
        String name,
        Integer priority,
        TaskApiStatus status,
        OffsetDateTime deadline,
        OffsetDateTime createdAt,
        List<TaskDependencyResponse> dependencies
) {
}
