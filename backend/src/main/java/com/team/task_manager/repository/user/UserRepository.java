package com.team.task_manager.repository.user;

import java.util.Optional;

import com.team.task_manager.repository.user.entity.UserDto;

public interface UserRepository {
    Optional<UserDto> findByUsername(String username);

    UserDto create(String username, String passwordHash);
}
