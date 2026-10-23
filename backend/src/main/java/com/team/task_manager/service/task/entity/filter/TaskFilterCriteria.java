package com.team.task_manager.service.task.entity.filter;

import java.time.OffsetDateTime;

import com.team.task_manager.controller.error.exception.task.InvalidTaskFilterException;
import com.team.task_manager.jooq.generated.enums.TaskStatus;

public record TaskFilterCriteria(
        TaskStatus status,
        Integer priority,
        OffsetDateTime deadlineFrom,
        OffsetDateTime deadlineTo
) {
    public TaskFilterCriteria {
        if (deadlineFrom != null && deadlineTo != null && deadlineFrom.isAfter(deadlineTo)) {
            throw new InvalidTaskFilterException();
        }
    }
}
