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

    boolean dependencyExists(Long taskId, Long dependencyTaskId);

    List<List<Long>> findAllDependencyEdges();

    void addDependency(Long taskId, Long dependencyTaskId);

    List<Long> findDependencyIds(Long taskId);

    List<Long> findDependentIds(Long taskId);

    void addDependencyIfAbsent(Long taskId, Long dependencyTaskId);

    Optional<TaskDto> update(TaskDto task);

    boolean deleteById(Long taskId);
}
