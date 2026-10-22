package com.team.task_manager.service.security.entity;

import java.io.Serializable;

public record AuthenticatedUser(
        Long id,
        String username
) implements Serializable {
}
