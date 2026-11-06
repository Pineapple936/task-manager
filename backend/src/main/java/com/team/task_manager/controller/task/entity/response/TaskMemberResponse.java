package com.team.task_manager.controller.task.entity.response;

import com.team.task_manager.controller.task.entity.TaskMemberApiRole;

public record TaskMemberResponse(
        Long userId,
        String username,
        TaskMemberApiRole role
) {
}
