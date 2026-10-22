package com.team.task_manager.controller.auth;

import com.team.task_manager.controller.auth.entity.AuthMapper;
import com.team.task_manager.controller.auth.entity.request.LoginRequest;
import com.team.task_manager.service.security.entity.AuthenticatedUser;
import com.team.task_manager.service.auth.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void savesAuthenticatedUserInHttpSession() {
        AuthService authService = mock(AuthService.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(1L, "egor");
        when(authenticationManager.authenticate(any())).thenReturn(
                UsernamePasswordAuthenticationToken.authenticated(
                        authenticatedUser,
                        null,
                        java.util.List.of()
                )
        );
        AuthController controller = new AuthController(
                authService,
                new AuthMapper(),
                authenticationManager,
                new HttpSessionSecurityContextRepository()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.login(new LoginRequest("egor", "password123"), request, response);

        assertNotNull(request.getSession(false));
        SecurityContext context = (SecurityContext) request.getSession(false).getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );
        assertNotNull(context);
        AuthenticatedUser principal = (AuthenticatedUser) context.getAuthentication().getPrincipal();
        assertEquals(1L, principal.id());
        assertEquals("egor", principal.username());
    }
}
