package com.team.task_manager.repository.task;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.repository.task.entity.TaskDto;

public interface TaskRepository {
    List<TaskDto> findAllByUserId(Long userId);

    Optional<TaskDto> findById(Long taskId);

    Optional<TaskMemberRole> findMemberRole(Long taskId, Long userId);

    TaskDto create(TaskDto task);

    void addOwner(Long taskId, Long userId);

    Optional<TaskDto> update(TaskDto task);

    boolean deleteById(Long taskId);
}
