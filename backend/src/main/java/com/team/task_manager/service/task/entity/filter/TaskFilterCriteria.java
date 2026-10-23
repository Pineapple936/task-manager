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
        if (priority != null && (priority < 0 || priority > 10)) {
            throw new InvalidTaskFilterException("Priority must be between 0 and 10");
        }

        if (deadlineFrom != null && deadlineTo != null && deadlineFrom.isAfter(deadlineTo)) {
            throw new InvalidTaskFilterException("The start date cannot be later than the end date");
        }
    }
}
