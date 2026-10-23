package com.team.task_manager.controller.task.entity.response;

import com.team.task_manager.controller.task.entity.TaskApiStatus;

public record TaskDependencyResponse(
        Long id,
        String name,
        TaskApiStatus status
) {
}
