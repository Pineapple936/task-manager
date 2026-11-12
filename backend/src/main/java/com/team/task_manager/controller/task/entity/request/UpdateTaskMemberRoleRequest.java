package com.team.task_manager.controller.task.entity.request;

import com.team.task_manager.controller.task.entity.TaskMemberApiRole;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskMemberRoleRequest(
        @NotNull(message = "Роль обязательна")
        TaskMemberApiRole role
) {
}
