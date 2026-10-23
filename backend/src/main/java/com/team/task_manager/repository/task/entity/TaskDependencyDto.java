package com.team.task_manager.repository.task.entity;

import com.team.task_manager.jooq.generated.enums.TaskStatus;

public record TaskDependencyDto(
        Long id,
        String name,
        TaskStatus status
) {
}
