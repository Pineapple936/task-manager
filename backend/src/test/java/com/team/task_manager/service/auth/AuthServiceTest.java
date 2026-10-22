package com.team.task_manager.service.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

import com.team.task_manager.controller.error.exception.auth.UsernameAlreadyExistsException;
import com.team.task_manager.repository.user.UserRepository;
import com.team.task_manager.repository.user.entity.UserDto;
import com.team.task_manager.service.auth.entity.CredentialsDto;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @Test
    void hashesPasswordWhenRegisteringUser() {
        RecordingUserRepository repository = new RecordingUserRepository();
        AuthService service = new AuthService(repository, new BCryptPasswordEncoder());

        UserDto user = service.register(new CredentialsDto("egor", "password123"));

        assertEquals("egor", user.username());
        assertNotEquals("password123", repository.passwordHash);
        assertTrue(new BCryptPasswordEncoder().matches("password123", repository.passwordHash));
    }

    @Test
    void rejectsDuplicateUsername() {
        RecordingUserRepository repository = new RecordingUserRepository();
        repository.user = Optional.of(user("hash"));
        AuthService service = new AuthService(repository, new BCryptPasswordEncoder());

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> service.register(new CredentialsDto("egor", "password123"))
        );
    }

    private static UserDto user(String passwordHash) {
        return new UserDto(1L, "egor", passwordHash, OffsetDateTime.now());
    }

    private static final class RecordingUserRepository implements UserRepository {
        private Optional<UserDto> user = Optional.empty();
        private String passwordHash;

        @Override
        public Optional<UserDto> findByUsername(String username) {
            return user;
        }

        @Override
        public UserDto create(String username, String passwordHash) {
            this.passwordHash = passwordHash;
            return new UserDto(1L, username, passwordHash, OffsetDateTime.now());
        }
    }
}
