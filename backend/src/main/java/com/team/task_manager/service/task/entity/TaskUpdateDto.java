package com.team.task_manager.service.task.entity;

import java.time.OffsetDateTime;

import com.team.task_manager.jooq.generated.enums.TaskStatus;

public record TaskUpdateDto(
        String name,
        String description,
        Integer priority,
        TaskStatus status,
        OffsetDateTime deadline,
        boolean deadlineSpecified
) {
}
