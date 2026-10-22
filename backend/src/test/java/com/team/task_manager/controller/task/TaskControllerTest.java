package com.team.task_manager.controller.task;

import java.time.OffsetDateTime;

import com.team.task_manager.controller.error.GlobalExceptionHandler;
import com.team.task_manager.controller.task.entity.TaskMapper;
import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.service.security.entity.AuthenticatedUser;
import com.team.task_manager.service.task.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerTest {
    private static final Long USER_ID = 10L;

    private TaskService taskService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        TaskController controller = new TaskController(taskService, new TaskMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        AuthenticatedUser principal = new AuthenticatedUser(USER_ID, "egor");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, java.util.List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validatesRequestBeforeCallingService() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"priority\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createsTaskForAuthenticatedUserAndReturnsApiResponse() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-23T00:00:00+03:00");
        when(taskService.create(any(), eq(USER_ID))).thenReturn(new TaskDto(
                1L,
                USER_ID,
                "Task",
                "Description",
                5,
                TaskStatus.planned,
                null,
                createdAt,
                createdAt
        ));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Task\",\"description\":\"Description\",\"priority\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("planned"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(taskService).create(any(), eq(USER_ID));
    }
}
