package com.team.task_manager.controller.auth.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Имя пользователя не должно быть пустым")
        @Size(max = 50, message = "Имя пользователя должно содержать не больше 50 символов")
        String username,

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 8, max = 72, message = "Пароль должен содержать от 8 до 72 символов")
        String password
) {
}
