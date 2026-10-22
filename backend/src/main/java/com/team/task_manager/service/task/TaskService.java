package com.team.task_manager.service.task;

import java.util.List;

import com.team.task_manager.controller.error.exception.task.TaskAccessDeniedException;
import com.team.task_manager.controller.error.exception.task.TaskNotFoundException;
import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.repository.task.TaskRepository;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.service.task.entity.TaskUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<TaskDto> findAll(Long userId) {
        return taskRepository.findAllByUserId(userId);
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
                task.updatedAt()
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

}
