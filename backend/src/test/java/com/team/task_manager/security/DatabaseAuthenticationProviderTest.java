package com.team.task_manager.security;

import java.time.OffsetDateTime;
import java.util.Optional;

import com.team.task_manager.repository.user.UserRepository;
import com.team.task_manager.repository.user.entity.UserDto;
import com.team.task_manager.service.security.entity.AuthenticatedUser;
import com.team.task_manager.service.security.DatabaseAuthenticationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseAuthenticationProviderTest {

    @Test
    void authenticatesUserWithCorrectPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserRepository repository = repositoryWithPassword(encoder.encode("password123"));
        DatabaseAuthenticationProvider provider = new DatabaseAuthenticationProvider(repository, encoder);

        Authentication authentication = provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("egor", "password123")
        );

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assertEquals(1L, principal.id());
        assertEquals("egor", principal.username());
    }

    @Test
    void rejectsIncorrectPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserRepository repository = repositoryWithPassword(encoder.encode("password123"));
        DatabaseAuthenticationProvider provider = new DatabaseAuthenticationProvider(repository, encoder);

        assertThrows(
                BadCredentialsException.class,
                () -> provider.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated("egor", "incorrect")
                )
        );
    }

    private static UserRepository repositoryWithPassword(String passwordHash) {
        UserDto user = new UserDto(1L, "egor", passwordHash, OffsetDateTime.now());
        return new UserRepository() {
            @Override
            public Optional<UserDto> findByUsername(String username) {
                return Optional.of(user);
            }

            @Override
            public UserDto create(String username, String passwordHash) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
