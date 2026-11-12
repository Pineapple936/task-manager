package com.team.task_manager.controller.task;

import java.time.OffsetDateTime;
import java.util.List;

import com.team.task_manager.controller.task.entity.TaskApiStatus;
import com.team.task_manager.controller.task.entity.TaskMapper;
import com.team.task_manager.controller.task.entity.request.CreateTaskRequest;
import com.team.task_manager.controller.task.entity.request.AddTaskMemberRequest;
import com.team.task_manager.controller.task.entity.request.UpdateTaskMemberRoleRequest;
import com.team.task_manager.controller.task.entity.request.UpdateTaskRequest;
import com.team.task_manager.controller.task.entity.response.TaskDetailsResponse;
import com.team.task_manager.controller.task.entity.response.TaskListItemResponse;
import com.team.task_manager.controller.task.entity.response.TaskMemberResponse;
import com.team.task_manager.controller.task.entity.response.TaskGraphResponse;
import com.team.task_manager.service.security.entity.AuthenticatedUser;
import com.team.task_manager.service.task.TaskService;
import com.team.task_manager.service.task.TaskMemberService;
import com.team.task_manager.service.task.entity.filter.TaskFilterCriteria;
import com.team.task_manager.service.task.entity.sort.SortAlgorithm;
import com.team.task_manager.service.task.entity.sort.SortDirection;
import com.team.task_manager.service.task.entity.sort.TaskSortField;
import com.team.task_manager.service.task.entity.sort.TopNAlgorithm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {
    private final TaskService taskService;
    private final TaskMemberService taskMemberService;
    private final TaskMapper taskMapper;

    @GetMapping
    public List<TaskListItemResponse> findAll(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) TaskApiStatus status,
            @RequestParam(required = false)
            @Min(value = 0, message = "Приоритет должен быть от 0 до 10")
            @Max(value = 10, message = "Приоритет должен быть от 0 до 10") Integer priority,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime deadlineFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime deadlineTo,
            @RequestParam(required = false) TaskSortField sortBy,
            @RequestParam(defaultValue = "ASC") SortDirection direction,
            @RequestParam(defaultValue = "MERGE") SortAlgorithm algorithm
    ) {
        return taskService.findAll(
                        user.id(),
                        new TaskFilterCriteria(
                                taskMapper.toDatabaseStatus(status),
                                priority,
                                deadlineFrom,
                                deadlineTo
                        ),
                        sortBy,
                        direction,
                        algorithm
                ).stream()
                .map(taskMapper::toListItemResponse)
                .toList();
    }

    @GetMapping("/{taskId}")
    public TaskDetailsResponse findById(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return taskMapper.toDetailsResponse(
                taskService.findById(taskId, user.id()),
                taskMemberService.findAll(taskId, user.id())
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDetailsResponse create(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        var createdTask = taskService.create(taskMapper.toCreateDto(request), user.id());
        return taskMapper.toDetailsResponse(
                createdTask,
                taskMemberService.findAll(createdTask.id(), user.id())
        );
    }

    @GetMapping("/{taskId}/members")
    public List<TaskMemberResponse> findMembers(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return taskMemberService.findAll(taskId, user.id()).stream()
                .map(taskMapper::toMemberResponse)
                .toList();
    }

    @PostMapping("/{taskId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMember(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @Valid @RequestBody AddTaskMemberRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskMemberService.add(
                taskId,
                request.userId(),
                taskMapper.toDatabaseRole(request.role()),
                user.id()
        );
    }

    @PatchMapping("/{taskId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMemberRole(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @PathVariable @Min(value = 1, message = "Идентификатор пользователя должен быть положительным") Long userId,
            @Valid @RequestBody UpdateTaskMemberRoleRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskMemberService.updateRole(
                taskId,
                userId,
                taskMapper.toDatabaseRole(request.role()),
                user.id()
        );
    }

    @DeleteMapping("/{taskId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @PathVariable @Min(value = 1, message = "Идентификатор пользователя должен быть положительным") Long userId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskMemberService.delete(taskId, userId, user.id());
    }

    @PostMapping("/{taskId}/dependencies/{dependencyTaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addDependency(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long dependencyTaskId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskService.addDependency(taskId, dependencyTaskId, user.id());
    }

    @DeleteMapping("/{taskId}/dependencies/{dependencyTaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDependency(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long dependencyTaskId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskService.deleteDependency(taskId, dependencyTaskId, user.id());
    }

    @PostMapping("/{taskId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addTag(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @PathVariable @Min(value = 1, message = "Идентификатор тега должен быть положительным") Long tagId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskService.addTag(taskId, tagId, user.id());
    }

    @DeleteMapping("/{taskId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @PathVariable @Min(value = 1, message = "Идентификатор тега должен быть положительным") Long tagId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskService.deleteTag(taskId, tagId, user.id());
    }

    @PatchMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskService.update(taskId, taskMapper.toUpdateDto(request), user.id());
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Min(value = 1, message = "Идентификатор задачи должен быть положительным") Long taskId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        taskService.delete(taskId, user.id());
    }

    @GetMapping("/graph")
    public TaskGraphResponse getTaskGraph(
        @AuthenticationPrincipal AuthenticatedUser user 
    ) {
        List<TaskListItemResponse> nodes = taskService.findAll(user.id()).stream()
                .map(taskMapper::toListItemResponse)
                .toList();
        
        return new TaskGraphResponse(nodes);
    }

    @GetMapping("/top-priority")
    public List<TaskListItemResponse> getTopNPriorityTasks(
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestParam(defaultValue = "5")
        @Min(value = 1, message = "N должно быть больше 0") int n,
        @RequestParam(defaultValue = "BST") TopNAlgorithm algorithm
    ) {
        return taskService.getTopNPriorityTasks(user.id(), n, algorithm).stream()
                .map(taskMapper::toListItemResponse)
                .toList();
    }

    @GetMapping("/recommended")
    public List<TaskListItemResponse> getRecommended(
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return taskService.getRecommendedOrder(user.id()).stream()
                .map(taskMapper::toListItemResponse)
                .toList();
    }
}
