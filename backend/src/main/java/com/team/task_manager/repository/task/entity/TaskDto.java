package com.team.task_manager.repository.task.entity;

import java.time.LocalDateTime;

import com.team.task_manager.jooq.generated.enums.TaskStatus;

public record TaskDto (
        Long id,
        Long createdByUserId,
        String name,
        String description,
        Integer priority,
        TaskStatus status,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
