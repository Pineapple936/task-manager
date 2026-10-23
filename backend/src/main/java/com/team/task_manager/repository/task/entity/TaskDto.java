package com.team.task_manager.repository.task.entity;

import java.time.OffsetDateTime;
import java.util.List;

import com.team.task_manager.jooq.generated.enums.TaskStatus;

public record TaskDto (
        Long id,
        Long createdByUserId,
        String name,
        String description,
        Integer priority,
        TaskStatus status,
        OffsetDateTime deadline,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<TaskDependencyDto> dependencies
) {
    public TaskDto(
            Long id,
            Long createdByUserId,
            String name,
            String description,
            Integer priority,
            TaskStatus status,
            OffsetDateTime deadline,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this(
                id,
                createdByUserId,
                name,
                description,
                priority,
                status,
                deadline,
                createdAt,
                updatedAt,
                List.of()
        );
    }

    public TaskDto {
        dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
    }

    public TaskDto withDependencies(List<TaskDependencyDto> dependencies) {
        return new TaskDto(
                id,
                createdByUserId,
                name,
                description,
                priority,
                status,
                deadline,
                createdAt,
                updatedAt,
                dependencies
        );
    }
}
