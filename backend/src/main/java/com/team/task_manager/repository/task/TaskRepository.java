package com.team.task_manager.repository.task;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.entity.TaskUpdateDto;

public interface TaskRepository {
    List<TaskDto> findAllByUserId(Long userId);

    Optional<TaskDto> findById(Long taskId);

    TaskDto create(TaskDto task);

    List<Long> createAll(List<TaskDto> tasks, Long userId);

    boolean dependencyExists(Long taskId, Long dependencyTaskId);

    List<List<Long>> findAllDependencyEdges();

    void addDependency(Long taskId, Long dependencyTaskId);

    boolean deleteDependency(Long taskId, Long dependencyTaskId);

    List<Long> findDependencyIds(Long taskId);

    List<Long> findDependentIds(Long taskId);

    void addDependencyIfAbsent(Long taskId, Long dependencyTaskId);

    boolean taskTagExists(Long taskId, Long tagId);

    void addTag(Long taskId, Long tagId);

    void deleteTag(Long taskId, Long tagId);

    Optional<TaskDto> update(Long taskId, TaskUpdateDto update);

    void deleteById(Long taskId);
}
