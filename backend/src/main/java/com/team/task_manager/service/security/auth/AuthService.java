package com.team.task_manager.service.security.auth;

import com.team.task_manager.controller.error.exception.auth.UsernameAlreadyExistsException;
import com.team.task_manager.repository.user.UserRepository;
import com.team.task_manager.repository.user.entity.UserDto;
import com.team.task_manager.service.security.auth.entity.CredentialsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto register(CredentialsDto credentials) {
        userRepository.findByUsername(credentials.username())
                .ifPresent(user -> {
                    throw new UsernameAlreadyExistsException(credentials.username());
                });

        return userRepository.create(
                credentials.username(),
                passwordEncoder.encode(credentials.password())
        );
    }
}
