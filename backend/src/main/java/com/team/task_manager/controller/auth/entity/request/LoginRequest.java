package com.team.task_manager.controller.auth.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Имя пользователя не должно быть пустым")
        @Size(max = 50, message = "Имя пользователя должно содержать не больше 50 символов")
        String username,

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 4, message = "Пароль должен содержать минимум 4 символа")
        String password
) {
}
