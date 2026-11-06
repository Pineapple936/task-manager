package com.team.task_manager.controller.task.entity.request;

import com.team.task_manager.controller.task.entity.TaskMemberApiRole;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddTaskMemberRequest(
        @NotNull(message = "Идентификатор пользователя обязателен")
        @Min(value = 1, message = "Идентификатор пользователя должен быть положительным")
        Long userId,
        @NotNull(message = "Роль обязательна")
        TaskMemberApiRole role
) {
}
