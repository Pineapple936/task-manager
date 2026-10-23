package com.team.task_manager.controller.statistics;

import com.team.task_manager.controller.error.GlobalExceptionHandler;
import com.team.task_manager.service.security.entity.AuthenticatedUser;
import com.team.task_manager.service.statistics.StatisticsService;
import com.team.task_manager.service.statistics.entity.TaskStatisticsDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatisticsControllerTest {
    private static final Long USER_ID = 10L;

    private StatisticsService statisticsService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        statisticsService = mock(StatisticsService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new StatisticsController(statisticsService))
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
    void returnsStatisticsForAuthenticatedUser() throws Exception {
        when(statisticsService.getStatistics(USER_ID)).thenReturn(
                new TaskStatisticsDto(2, 1, 3)
        );

        mockMvc.perform(get("/statistics/task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planned").value(2))
                .andExpect(jsonPath("$.started").value(1))
                .andExpect(jsonPath("$.completed").value(3));

        verify(statisticsService).getStatistics(USER_ID);
    }
}
