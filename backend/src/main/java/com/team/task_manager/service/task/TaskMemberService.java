package com.team.task_manager.service.task;

import java.util.List;

import com.team.task_manager.controller.error.exception.task.InvalidTaskMemberRoleException;
import com.team.task_manager.controller.error.exception.task.TaskAccessDeniedException;
import com.team.task_manager.controller.error.exception.task.TaskMemberAlreadyExistsException;
import com.team.task_manager.controller.error.exception.task.TaskMemberNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskNotFoundException;
import com.team.task_manager.controller.error.exception.user.UserNotFoundException;
import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.repository.task.TaskRepository;
import com.team.task_manager.repository.task.entity.TaskMemberDto;
import com.team.task_manager.repository.task.member.TaskMemberRepository;
import com.team.task_manager.repository.user.UserRepository;
import com.team.task_manager.repository.user.entity.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskMemberService {
    private final TaskRepository taskRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaskMemberDto> findAll(Long taskId, Long currentUserId) {
        requireTask(taskId);
        requireAccess(taskId, currentUserId);
        return taskMemberRepository.findAll(taskId);
    }

    @Transactional
    public void addOwner(Long taskId, Long userId) {
        taskMemberRepository.add(taskId, userId, TaskMemberRole.owner);
    }

    @Transactional
    public void addOwners(List<Long> taskIds, Long userId) {
        taskMemberRepository.addOwners(taskIds, userId);
    }

    @Transactional(readOnly = true)
    public void requireAccess(Long taskId, Long userId) {
        requireMember(taskId, userId);
    }

    @Transactional(readOnly = true)
    public void requireEditor(Long taskId, Long userId) {
        TaskMemberRole role = requireMember(taskId, userId);
        if (role != TaskMemberRole.owner && role != TaskMemberRole.editor) {
            throw new TaskAccessDeniedException(taskId);
        }
    }

    @Transactional(readOnly = true)
    public void requireOwner(Long taskId, Long userId) {
        if (requireMember(taskId, userId) != TaskMemberRole.owner) {
            throw new TaskAccessDeniedException(taskId);
        }
    }

    @Transactional
    public void add(Long taskId, Long userId, TaskMemberRole role, Long currentUserId) {
        requireTaskOwner(taskId, currentUserId);
        requireAssignableRole(role);

        UserDto user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (taskMemberRepository.findRole(taskId, user.id()).isPresent()) {
            throw new TaskMemberAlreadyExistsException(taskId, user.id());
        }

        taskMemberRepository.add(taskId, user.id(), role);
    }

    @Transactional
    public void updateRole(Long taskId, Long userId, TaskMemberRole role, Long currentUserId) {
        requireTaskOwner(taskId, currentUserId);
        requireAssignableRole(role);

        TaskMemberRole currentRole = taskMemberRepository.findRole(taskId, userId)
                .orElseThrow(() -> new TaskMemberNotFoundException(taskId, userId));
        if (currentRole == TaskMemberRole.owner) {
            throw new InvalidTaskMemberRoleException();
        }

        if (!taskMemberRepository.updateRole(taskId, userId, role)) {
            throw new TaskMemberNotFoundException(taskId, userId);
        }
    }

    @Transactional
    public void delete(Long taskId, Long userId, Long currentUserId) {
        requireTaskOwner(taskId, currentUserId);

        TaskMemberRole currentRole = taskMemberRepository.findRole(taskId, userId)
                .orElseThrow(() -> new TaskMemberNotFoundException(taskId, userId));
        if (currentRole == TaskMemberRole.owner) {
            throw new InvalidTaskMemberRoleException();
        }

        if (!taskMemberRepository.delete(taskId, userId)) {
            throw new TaskMemberNotFoundException(taskId, userId);
        }
    }

    private void requireTask(Long taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private TaskMemberRole requireMember(Long taskId, Long userId) {
        return taskMemberRepository.findRole(taskId, userId)
                .orElseThrow(() -> new TaskAccessDeniedException(taskId));
    }

    private void requireTaskOwner(Long taskId, Long userId) {
        requireTask(taskId);
        requireOwner(taskId, userId);
    }

    private static void requireAssignableRole(TaskMemberRole role) {
        if (role == TaskMemberRole.owner) {
            throw new InvalidTaskMemberRoleException();
        }
    }
}
