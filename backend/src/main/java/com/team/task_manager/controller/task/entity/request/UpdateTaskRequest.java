package com.team.task_manager.controller.task.entity.request;

import java.time.OffsetDateTime;

import com.team.task_manager.controller.task.entity.TaskApiStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record UpdateTaskRequest(
        JsonNullable<
                @NotNull(message = "Название не может быть null")
                @Size(max = 255, message = "Название должно содержать от 1 до 255 символов")
                String> name,

        JsonNullable<
                @NotNull(message = "Описание не может быть null")
                @Size(max = 5000, message = "Описание должно содержать не больше 5000 символов")
                String> description,

        JsonNullable<
                @NotNull(message = "Приоритет не может быть null")
                @Positive(message = "Приоритет должен быть положительным")
                Integer> priority,

        JsonNullable<
                @NotNull(message = "Статус не может быть null")
                TaskApiStatus> status,

        JsonNullable<OffsetDateTime> deadline
) {
}
