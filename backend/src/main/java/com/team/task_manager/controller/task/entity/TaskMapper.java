package com.team.task_manager.controller.task.entity;

import com.team.task_manager.controller.task.entity.request.CreateTaskRequest;
import com.team.task_manager.controller.task.entity.request.UpdateTaskRequest;
import com.team.task_manager.controller.task.entity.response.TaskDetailsResponse;
import com.team.task_manager.controller.task.entity.response.TaskDependencyResponse;
import com.team.task_manager.controller.task.entity.response.TaskListItemResponse;
import com.team.task_manager.controller.task.entity.response.TaskMemberResponse;
import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.entity.TaskMemberDto;
import com.team.task_manager.repository.task.entity.TaskUpdateDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDto toCreateDto(CreateTaskRequest request) {
        return new TaskDto(
                null,
                null,
                request.name(),
                request.description() == null ? "" : request.description(),
                request.priority(),
                TaskStatus.planned,
                request.deadline(),
                null,
                null
        );
    }

    public TaskUpdateDto toUpdateDto(UpdateTaskRequest request) {
        return new TaskUpdateDto(
                request.name().orElse(null),
                request.description().orElse(null),
                request.priority().orElse(null),
                toDatabaseStatus(request.status().orElse(null)),
                request.deadline().orElse(null),
                request.deadline().isPresent()
        );
    }

    public TaskListItemResponse toListItemResponse(TaskDto task) {
        return new TaskListItemResponse(
                task.id(),
                task.name(),
                task.priority(),
                toApiStatus(task.status()),
                task.deadline(),
                task.createdAt(),
                task.dependencies().stream()
                        .map(dependency -> new TaskDependencyResponse(
                                dependency.id(),
                                dependency.name(),
                                toApiStatus(dependency.status())
                        ))
                        .toList()
        );
    }

    public TaskDetailsResponse toDetailsResponse(TaskDto task) {
        return toDetailsResponse(task, List.of());
    }

    public TaskDetailsResponse toDetailsResponse(TaskDto task, List<TaskMemberDto> members) {
        return new TaskDetailsResponse(
                task.id(),
                task.name(),
                task.priority(),
                toApiStatus(task.status()),
                task.deadline(),
                task.createdAt(),
                task.description(),
                task.dependencies().stream()
                        .map(dependency -> new TaskDependencyResponse(
                                dependency.id(),
                                dependency.name(),
                                toApiStatus(dependency.status())
                        ))
                        .toList(),
                members.stream()
                        .map(this::toMemberResponse)
                        .toList()
        );
    }

    public TaskMemberResponse toMemberResponse(TaskMemberDto member) {
        return new TaskMemberResponse(
                member.userId(),
                member.username(),
                TaskMemberApiRole.valueOf(member.role().getLiteral())
        );
    }

    public TaskMemberRole toDatabaseRole(TaskMemberApiRole role) {
        return TaskMemberRole.valueOf(role.name());
    }

    public TaskStatus toDatabaseStatus(TaskApiStatus status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case planned -> TaskStatus.planned;
            case started -> TaskStatus.started;
            case completed -> TaskStatus.completed;
        };
    }

    private static TaskApiStatus toApiStatus(TaskStatus status) {
        return switch (status) {
            case planned -> TaskApiStatus.planned;
            case started -> TaskApiStatus.started;
            case completed -> TaskApiStatus.completed;
        };
    }
}
