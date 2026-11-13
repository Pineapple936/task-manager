package com.team.task_manager.service.task;

import java.util.List;

import com.team.task_manager.controller.error.exception.task.TaskCompletionBlockedException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyAlreadyExistsException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyCycleException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskSelfDependencyException;
import com.team.task_manager.controller.error.exception.tag.TaskTagAlreadyExistsException;
import com.team.task_manager.repository.task.TaskRepository;
import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.entity.TaskUpdateDto;
import com.team.task_manager.service.algorithm.graph.AdjacencyMatrixGraph;
import com.team.task_manager.service.task.entity.filter.TaskFilterCriteria;
import com.team.task_manager.service.task.entity.sort.SortAlgorithm;
import com.team.task_manager.service.task.entity.sort.SortDirection;
import com.team.task_manager.service.task.entity.sort.TaskSortField;
import com.team.task_manager.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TagService tagService;
    private final TaskMemberService taskMemberService;
    private final TaskFilter taskFilter;
    private final TaskSorter taskSorter;

    @Transactional(readOnly = true)
    public List<TaskDto> findAll(Long userId) {
        return taskRepository.findAllByUserId(userId);
    }

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

        taskMemberService.requireAccess(taskId, userId);

        return task;
    }

    @Transactional
    public void createAll(List<TaskDto> tasks, Long userId) {
        if (tasks.isEmpty()) {
            return;
        }
        List<Long> taskIds = taskRepository.createAll(tasks, userId);
        taskMemberService.addOwners(taskIds, userId);
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
        taskMemberService.addOwner(createdTask.id(), userId);

        return createdTask;
    }

    @Transactional
    public TaskDto update(Long taskId, TaskUpdateDto update, Long userId) {
        TaskDto task = requireTaskEditor(taskId, userId);

        if (update.status() == TaskStatus.completed
                && task.dependencies().stream().anyMatch(dependency -> dependency.status() != TaskStatus.completed)) {
            throw new TaskCompletionBlockedException(taskId);
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
                task.dependencies(),
                task.tags()
        );

        return taskRepository.update(updatedTask)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Transactional
    public void delete(Long taskId, Long userId) {
        if (taskRepository.findById(taskId).isEmpty()) {
            return;
        }

        taskMemberService.requireOwner(taskId, userId);

        List<Long> dependencyIds = taskRepository.findDependencyIds(taskId);
        List<Long> dependentIds = taskRepository.findDependentIds(taskId);
        for (Long dependentTaskId : dependentIds) {
            for (Long dependencyId : dependencyIds) {
                if (!dependentTaskId.equals(dependencyId)) {
                    taskRepository.addDependencyIfAbsent(dependentTaskId, dependencyId);
                }
            }
        }

        taskRepository.deleteById(taskId);
    }

    @Transactional
    public void addDependency(Long taskId, Long dependencyTaskId, Long userId) {
        if (taskId.equals(dependencyTaskId)) {
            throw new TaskSelfDependencyException();
        }

        requireTaskEditor(taskId, userId);
        taskRepository.findById(dependencyTaskId)
                .orElseThrow(() -> new TaskNotFoundException(dependencyTaskId));

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

    @Transactional
    public void deleteDependency(Long taskId, Long dependencyTaskId, Long userId) {
        requireTaskEditor(taskId, userId);

        if (!taskRepository.deleteDependency(taskId, dependencyTaskId)) {
            throw new TaskDependencyNotFoundException(taskId, dependencyTaskId);
        }
    }

    @Transactional
    public void addTag(Long taskId, Long tagId, Long userId) {
        requireTaskEditor(taskId, userId);
        tagService.findById(tagId);

        if (taskRepository.taskTagExists(taskId, tagId)) {
            throw new TaskTagAlreadyExistsException(taskId, tagId);
        }

        taskRepository.addTag(taskId, tagId);
    }

    @Transactional
    public void deleteTag(Long taskId, Long tagId, Long userId) {
        requireTaskEditor(taskId, userId);
        tagService.findById(tagId);

        taskRepository.deleteTag(taskId, tagId);
    }

    private TaskDto requireTaskEditor(Long taskId, Long userId) {
        TaskDto task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        taskMemberService.requireEditor(taskId, userId);

        return task;
    }

}
