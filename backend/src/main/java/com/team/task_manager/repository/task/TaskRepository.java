package com.team.task_manager.repository.task;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.repository.task.entity.TaskDto;

public interface TaskRepository {
    List<TaskDto> findAllByUserId(Long userId);

    Optional<TaskDto> findByIdAndUserId(Long taskId, Long userId);

    TaskDto create(TaskDto task);

    Optional<TaskDto> update(TaskDto task, Long userId);

    boolean deleteByIdAndUserId(Long taskId, Long userId);
}
