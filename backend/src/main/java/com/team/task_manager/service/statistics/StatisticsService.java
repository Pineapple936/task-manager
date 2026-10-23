package com.team.task_manager.service.statistics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.service.statistics.entity.TaskStatisticsDto;
import com.team.task_manager.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final TaskService taskService;

    @Transactional(readOnly = true)
    public TaskStatisticsDto getStatistics(Long userId) {
        List<TaskDto> tasks = taskService.findAll(userId);

        long planned = 0;
        long started = 0;
        long completed = 0;
        Map<Integer, Long> priorityDistribution = new LinkedHashMap<>();
        for (int priority = 0; priority <= 10; priority++) {
            priorityDistribution.put(priority, 0L);
        }

        for (TaskDto task : tasks) {
            switch (task.status()) {
                case planned -> planned++;
                case started -> started++;
                case completed -> completed++;
            }
            priorityDistribution.put(
                    task.priority(),
                    priorityDistribution.get(task.priority()) + 1
            );
        }

        return new TaskStatisticsDto(
                tasks.size(),
                completed,
                started,
                planned,
                priorityDistribution
        );
    }
}
