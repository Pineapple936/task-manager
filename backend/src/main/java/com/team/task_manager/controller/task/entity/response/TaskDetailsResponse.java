package com.team.task_manager.controller.task.entity.response;

import java.time.OffsetDateTime;
import java.util.List;

import com.team.task_manager.controller.task.entity.TaskApiStatus;
import com.team.task_manager.controller.tag.entity.response.TagResponse;

public record TaskDetailsResponse(
        Long id,
        String name,
        Integer priority,
        TaskApiStatus status,
        OffsetDateTime deadline,
        OffsetDateTime createdAt,
        String description,
        List<TaskDependencyResponse> dependencies,
        List<TagResponse> tags
) {
}
