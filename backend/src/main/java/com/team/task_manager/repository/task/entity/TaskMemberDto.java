package com.team.task_manager.repository.task.entity;

import com.team.task_manager.jooq.generated.enums.TaskMemberRole;

public record TaskMemberDto(
        Long taskId,
        Long userId,
        String username,
        TaskMemberRole role
) {
}
