package com.team.task_manager.controller.task.entity.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Название не должно быть пустым")
        @Size(max = 255, message = "Название должно содержать не больше 255 символов")
        String name,

        @Size(max = 5000, message = "Описание должно содержать не больше 5000 символов")
        String description,

        @NotNull(message = "Приоритет обязателен")
        Integer priority,

        OffsetDateTime deadline
) {
}
