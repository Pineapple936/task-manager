package com.team.task_manager.repository.task.entity;

import java.time.OffsetDateTime;
import java.util.List;

import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.tag.entity.TagDto;

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
        List<TaskDependencyDto> dependencies,
        List<TagDto> tags
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
                List.of(),
                List.of()
        );
    }

    public TaskDto(
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
                dependencies,
                List.of()
        );
    }

    public TaskDto {
        dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
        tags = tags == null ? List.of() : List.copyOf(tags);
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
                dependencies,
                tags
        );
    }

    public TaskDto withTags(List<TagDto> tags) {
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
                dependencies,
                tags
        );
    }
}
