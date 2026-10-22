package com.team.task_manager.repository.user.entity;

import java.time.OffsetDateTime;

public record UserDto(
        Long id,
        String username,
        String passwordHash,
        OffsetDateTime createdAt
) {
}
