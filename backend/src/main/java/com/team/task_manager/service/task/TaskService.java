package com.team.task_manager.service.task;

import java.util.List;

import com.team.task_manager.controller.error.exception.task.TaskAccessDeniedException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyAlreadyExistsException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyCycleException;
import com.team.task_manager.controller.error.exception.task.TaskNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskSelfDependencyException;
import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.repository.task.TaskRepository;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.entity.TaskUpdateDto;
import com.team.task_manager.service.algorithm.graph.AdjacencyMatrixGraph;
import com.team.task_manager.service.task.entity.filter.TaskFilterCriteria;
import com.team.task_manager.service.task.entity.sort.SortAlgorithm;
import com.team.task_manager.service.task.entity.sort.SortDirection;
import com.team.task_manager.service.task.entity.sort.TaskSortField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskFilter taskFilter;
    private final TaskSorter taskSorter;

    @Transactional(readOnly = true)
    public List<TaskDto> findAll(
            Long userId,
            TaskFilterCriteria filterCriteria,
            TaskSortField sortField,
            SortDirection sortDirection,
            SortAlgorithm sortAlgorithm
    ) {
        List<TaskDto> filteredTasks = taskFilter.filter(
                taskRepository.findAllByUserId(userId),
                filterCriteria
        );

        if (sortField == null) {
            return filteredTasks;
        }

        return taskSorter.sort(filteredTasks, sortAlgorithm, sortField, sortDirection);
    }

    @Transactional(readOnly = true)
    public TaskDto findById(Long taskId, Long userId) {
        TaskDto task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        taskRepository.findMemberRole(taskId, userId)
                .orElseThrow(() -> new TaskAccessDeniedException(taskId));

        return task;
    }

    @Transactional
    public TaskDto create(TaskDto task, Long userId) {
        TaskDto createdTask = taskRepository.create(
                new TaskDto(
                    null,
                    userId,
                    task.name(),
                    task.description(),
                    task.priority(),
                    task.status(),
                    task.deadline(),
                    null,
                    null
                )
        );
        taskRepository.addOwner(createdTask.id(), userId);

        return createdTask;
    }

    @Transactional
    public TaskDto update(Long taskId, TaskUpdateDto update, Long userId) {
        TaskDto task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskMemberRole role = taskRepository.findMemberRole(taskId, userId)
                .orElseThrow(() -> new TaskAccessDeniedException(taskId));
        if (role != TaskMemberRole.owner && role != TaskMemberRole.editor) {
            throw new TaskAccessDeniedException(taskId);
        }

        TaskDto updatedTask = new TaskDto(
                task.id(),
                task.createdByUserId(),
                update.name() != null ? update.name() : task.name(),
                update.description() != null ? update.description() : task.description(),
                update.priority() != null ? update.priority() : task.priority(),
                update.status() != null ? update.status() : task.status(),
                update.deadlineSpecified() ? update.deadline() : task.deadline(),
                task.createdAt(),
                task.updatedAt(),
                task.dependencies()
        );

        return taskRepository.update(updatedTask)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Transactional
    public void delete(Long taskId, Long userId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskMemberRole role = taskRepository.findMemberRole(taskId, userId)
                .orElseThrow(() -> new TaskAccessDeniedException(taskId));
        if (role != TaskMemberRole.owner) {
            throw new TaskAccessDeniedException(taskId);
        }

        if (!taskRepository.deleteById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
    }

    @Transactional
    public void addDependency(Long taskId, Long dependencyTaskId, Long userId) {
        if (taskId.equals(dependencyTaskId)) {
            throw new TaskSelfDependencyException();
        }

        taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        taskRepository.findById(dependencyTaskId)
                .orElseThrow(() -> new TaskNotFoundException(dependencyTaskId));

        TaskMemberRole role = taskRepository.findMemberRole(taskId, userId)
                .orElseThrow(() -> new TaskAccessDeniedException(taskId));
        if (role != TaskMemberRole.owner && role != TaskMemberRole.editor) {
            throw new TaskAccessDeniedException(taskId);
        }

        if (taskRepository.dependencyExists(taskId, dependencyTaskId)) {
            throw new TaskDependencyAlreadyExistsException(taskId, dependencyTaskId);
        }

        AdjacencyMatrixGraph dependencyGraph = new AdjacencyMatrixGraph(
                taskRepository.findAllDependencyEdges()
        );
        if (dependencyGraph.hasPath(dependencyTaskId, taskId)) {
            throw new TaskDependencyCycleException(taskId, dependencyTaskId);
        }

        taskRepository.addDependency(taskId, dependencyTaskId);
    }

}
