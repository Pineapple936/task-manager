package com.team.task_manager.controller.statistics;

import com.team.task_manager.controller.statistics.entity.response.TaskStatisticsResponse;
import com.team.task_manager.service.security.entity.AuthenticatedUser;
import com.team.task_manager.service.statistics.StatisticsService;
import com.team.task_manager.service.statistics.entity.TaskStatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/task")
    public TaskStatisticsResponse getStatistics(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        TaskStatisticsDto statistics = statisticsService.getStatistics(user.id());
        return new TaskStatisticsResponse(
                statistics.planned(),
                statistics.started(),
                statistics.completed()
        );
    }
}
