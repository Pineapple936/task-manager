package com.team.task_manager.service.statistics;

import java.util.List;

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

        for (TaskDto task : tasks) {
            switch (task.status()) {
                case planned -> planned++;
                case started -> started++;
                case completed -> completed++;
            }
        }

        return new TaskStatisticsDto(planned, started, completed);
    }
}
