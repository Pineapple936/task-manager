package com.team.task_manager.controller.auth.entity;

import com.team.task_manager.controller.auth.entity.request.RegisterRequest;
import com.team.task_manager.controller.auth.entity.response.UserResponse;
import com.team.task_manager.repository.user.entity.UserDto;
import com.team.task_manager.service.auth.entity.CredentialsDto;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public CredentialsDto toDto(RegisterRequest request) {
        return new CredentialsDto(request.username(), request.password());
    }

    public UserResponse toResponse(UserDto user) {
        return new UserResponse(user.id(), user.username());
    }
}
