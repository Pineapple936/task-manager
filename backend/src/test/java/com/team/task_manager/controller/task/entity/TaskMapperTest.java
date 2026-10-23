package com.team.task_manager.controller.task.entity;

import java.time.OffsetDateTime;

import com.team.task_manager.controller.task.entity.request.CreateTaskRequest;
import com.team.task_manager.controller.task.entity.request.UpdateTaskRequest;
import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.entity.TaskDependencyDto;
import com.team.task_manager.repository.task.entity.TaskUpdateDto;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskMapperTest {
    private final TaskMapper mapper = new TaskMapper();

    @Test
    void mapsApiStatusToDatabaseStatus() {
        UpdateTaskRequest request = new UpdateTaskRequest(
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(TaskApiStatus.started),
                JsonNullable.undefined()
        );

        TaskUpdateDto update = mapper.toUpdateDto(request);

        assertEquals(TaskStatus.started, update.status());
    }

    @Test
    void mapsSpecifiedDeadline() {
        OffsetDateTime deadline = OffsetDateTime.now();
        UpdateTaskRequest request = new UpdateTaskRequest(
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(deadline)
        );

        TaskUpdateDto update = mapper.toUpdateDto(request);

        assertTrue(update.deadlineSpecified());
        assertEquals(deadline, update.deadline());
    }

    @Test
    void mapsExplicitNullDeadlineForClearing() {
        UpdateTaskRequest request = new UpdateTaskRequest(
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(null)
        );

        TaskUpdateDto update = mapper.toUpdateDto(request);

        assertTrue(update.deadlineSpecified());
        assertNull(update.deadline());
    }

    @Test
    void mapsDatabaseStatusToApiStatus() {
        TaskDto task = new TaskDto(
                1L,
                2L,
                "Task",
                "Description",
                5,
                TaskStatus.planned,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        assertEquals(TaskApiStatus.planned, mapper.toDetailsResponse(task).status());
    }

    @Test
    void appliesDefaultsToCreatedTask() {
        TaskDto task = mapper.toCreateDto(new CreateTaskRequest("Task", null, 5, null));

        assertEquals("", task.description());
        assertEquals(TaskStatus.planned, task.status());
    }

    @Test
    void mapsTaskDependenciesToApiResponse() {
        TaskDto task = new TaskDto(
                1L,
                2L,
                "Task",
                "Description",
                5,
                TaskStatus.planned,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                java.util.List.of(new TaskDependencyDto(3L, "Dependency", TaskStatus.completed))
        );

        var response = mapper.toDetailsResponse(task);

        assertEquals(1, response.dependencies().size());
        assertEquals(3L, response.dependencies().getFirst().id());
        assertEquals(TaskApiStatus.completed, response.dependencies().getFirst().status());
    }
}
