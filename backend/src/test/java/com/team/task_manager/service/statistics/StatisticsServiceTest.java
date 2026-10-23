package com.team.task_manager.service.statistics;

import java.util.List;

import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.service.statistics.entity.TaskStatisticsDto;
import com.team.task_manager.service.task.TaskService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatisticsServiceTest {
    private static final Long USER_ID = 10L;

    @Test
    void countsAvailableTasksByStatus() {
        TaskService taskService = mock(TaskService.class);
        when(taskService.findAll(USER_ID)).thenReturn(List.of(
                task(1L, TaskStatus.planned),
                task(2L, TaskStatus.planned),
                task(3L, TaskStatus.started),
                task(4L, TaskStatus.completed)
        ));
        StatisticsService statisticsService = new StatisticsService(taskService);

        TaskStatisticsDto statistics = statisticsService.getStatistics(USER_ID);

        assertEquals(4, statistics.total());
        assertEquals(1, statistics.completed());
        assertEquals(1, statistics.started());
        assertEquals(2, statistics.planned());
        assertEquals(4, statistics.priorityDistribution().get(0));
        assertEquals(0, statistics.priorityDistribution().get(10));
        assertEquals(11, statistics.priorityDistribution().size());
        assertEquals(
                statistics.total(),
                statistics.completed() + statistics.started() + statistics.planned()
        );
        assertEquals(
                statistics.total(),
                statistics.priorityDistribution().values().stream().mapToLong(Long::longValue).sum()
        );
        verify(taskService).findAll(USER_ID);
    }

    @Test
    void returnsZerosWhenUserHasNoTasks() {
        TaskService taskService = mock(TaskService.class);
        when(taskService.findAll(USER_ID)).thenReturn(List.of());
        StatisticsService statisticsService = new StatisticsService(taskService);

        TaskStatisticsDto statistics = statisticsService.getStatistics(USER_ID);

        assertEquals(0, statistics.total());
        assertEquals(0, statistics.completed());
        assertEquals(0, statistics.started());
        assertEquals(0, statistics.planned());
        assertEquals(11, statistics.priorityDistribution().size());
        assertEquals(
                0,
                statistics.priorityDistribution().values().stream().mapToLong(Long::longValue).sum()
        );
    }

    private static TaskDto task(Long id, TaskStatus status) {
        return new TaskDto(id, USER_ID, "Task", "", 0, status, null, null, null);
    }
}
