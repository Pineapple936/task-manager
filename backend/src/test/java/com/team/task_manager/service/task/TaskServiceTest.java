package com.team.task_manager.service.task;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.team.task_manager.controller.error.exception.task.TaskAccessDeniedException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyAlreadyExistsException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyCycleException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskSelfDependencyException;
import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.task.TaskRepository;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.entity.TaskUpdateDto;
import com.team.task_manager.service.task.entity.filter.TaskFilterCriteria;
import com.team.task_manager.service.task.entity.sort.SortAlgorithm;
import com.team.task_manager.service.task.entity.sort.SortDirection;
import com.team.task_manager.service.task.entity.sort.TaskSortField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TaskServiceTest {
    private static final Long USER_ID = 10L;
    private static final Long TASK_ID = 20L;

    @Test
    void createsTaskForUserAndAddsOwnerMembership() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        TaskDto createdTask = task(TASK_ID, USER_ID);
        repository.createdTask = createdTask;
        TaskService service = service(repository);

        TaskDto result = service.create(task(null, 999L), USER_ID);

        assertSame(createdTask, result);
        assertNull(repository.taskToCreate.id());
        assertEquals(USER_ID, repository.taskToCreate.createdByUserId());
        assertEquals(TASK_ID, repository.ownerTaskId);
        assertEquals(USER_ID, repository.ownerUserId);
    }

    @Test
    void returnsTasksAvailableToUser() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        List<TaskDto> tasks = List.of(task(TASK_ID, USER_ID));
        repository.tasks = tasks;
        TaskSorter taskSorter = mock(TaskSorter.class);
        TaskService service = new TaskService(repository, new TaskFilter(), taskSorter);

        assertEquals(
                tasks,
                service.findAll(
                        USER_ID,
                        new TaskFilterCriteria(null, null, null, null),
                        null,
                        SortDirection.ASC,
                        SortAlgorithm.MERGE
                )
        );
        verifyNoInteractions(taskSorter);
    }

    @Test
    void returnsAllTasksWithoutFiltersAndSorting() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        List<TaskDto> tasks = List.of(task(TASK_ID, USER_ID));
        repository.tasks = tasks;
        TaskSorter taskSorter = mock(TaskSorter.class);
        TaskService service = new TaskService(repository, new TaskFilter(), taskSorter);

        assertEquals(tasks, service.findAll(USER_ID));
        verifyNoInteractions(taskSorter);
    }

    @Test
    void filtersTasksByStatusPriorityAndDeadline() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        OffsetDateTime deadlineFrom = OffsetDateTime.parse("2026-10-01T00:00:00Z");
        OffsetDateTime deadlineTo = OffsetDateTime.parse("2026-10-31T23:59:59Z");
        TaskDto expectedTask = task(
                TASK_ID,
                USER_ID,
                TaskStatus.planned,
                3,
                OffsetDateTime.parse("2026-10-15T12:00:00Z")
        );
        repository.tasks = List.of(
                expectedTask,
                task(21L, USER_ID, TaskStatus.started, 3, expectedTask.deadline()),
                task(22L, USER_ID, TaskStatus.planned, 1, expectedTask.deadline()),
                task(23L, USER_ID, TaskStatus.planned, 3, null),
                task(24L, USER_ID, TaskStatus.planned, 3, OffsetDateTime.parse("2026-11-01T00:00:00Z"))
        );

        List<TaskDto> result = service(repository).findAll(
                USER_ID,
                new TaskFilterCriteria(TaskStatus.planned, 3, deadlineFrom, deadlineTo),
                TaskSortField.PRIORITY,
                SortDirection.ASC,
                SortAlgorithm.INSERTION
        );

        assertEquals(List.of(expectedTask), result);
    }

    @Test
    void returnsTaskAvailableToUser() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        TaskDto task = task(TASK_ID, USER_ID);
        repository.task = Optional.of(task);
        repository.memberRole = Optional.of(TaskMemberRole.viewer);
        TaskService service = service(repository);

        assertSame(task, service.findById(TASK_ID, USER_ID));
    }

    @Test
    void throwsWhenTaskIsMissing() {
        TaskService service = service(new RecordingTaskRepository());

        assertThrows(TaskNotFoundException.class, () -> service.findById(TASK_ID, USER_ID));
    }

    @Test
    void throwsWhenTaskViewIsNotAllowed() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        TaskService service = service(repository);

        assertThrows(TaskAccessDeniedException.class, () -> service.findById(TASK_ID, USER_ID));
    }

    @Test
    void throwsWhenUpdateIsNotAllowed() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.viewer);
        TaskService service = service(repository);

        assertThrows(TaskAccessDeniedException.class, () -> service.update(TASK_ID, update(), USER_ID));
    }

    @Test
    void throwsWhenUpdatedTaskIsMissing() {
        TaskService service = service(new RecordingTaskRepository());

        assertThrows(TaskNotFoundException.class, () -> service.update(TASK_ID, update(), USER_ID));
    }

    @Test
    void updatesTaskWhenUserHasAccess() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        TaskDto task = task(TASK_ID, USER_ID);
        repository.task = Optional.of(task);
        repository.memberRole = Optional.of(TaskMemberRole.editor);
        repository.updatedTask = Optional.of(task);
        TaskService service = service(repository);

        assertSame(task, service.update(TASK_ID, update(), USER_ID));
        assertEquals("Task", repository.taskToUpdate.name());
    }

    @Test
    void preservesUnspecifiedFieldsAndClearsDeadline() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        TaskDto existingTask = new TaskDto(
                TASK_ID,
                USER_ID,
                "Original",
                "Description",
                1,
                null,
                java.time.OffsetDateTime.now(),
                null,
                null
        );
        repository.task = Optional.of(existingTask);
        repository.memberRole = Optional.of(TaskMemberRole.owner);
        repository.updatedTask = Optional.of(existingTask);
        TaskService service = service(repository);

        service.update(
                TASK_ID,
                new TaskUpdateDto("Changed", null, null, null, null, true),
                USER_ID
        );

        assertEquals("Changed", repository.taskToUpdate.name());
        assertEquals("Description", repository.taskToUpdate.description());
        assertNull(repository.taskToUpdate.deadline());
    }

    @Test
    void throwsWhenDeleteIsNotAllowed() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.editor);
        TaskService service = service(repository);

        assertThrows(TaskAccessDeniedException.class, () -> service.delete(TASK_ID, USER_ID));
    }

    @Test
    void throwsWhenDeletedTaskIsMissing() {
        TaskService service = service(new RecordingTaskRepository());

        assertThrows(TaskNotFoundException.class, () -> service.delete(TASK_ID, USER_ID));
    }

    @Test
    void deletesTaskWhenUserHasAccess() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.owner);
        repository.dependencyIds = List.of(1L, 2L);
        repository.dependentIds = List.of(30L, 40L);
        repository.deleted = true;
        TaskService service = service(repository);

        service.delete(TASK_ID, USER_ID);

        assertEquals(
                List.of(
                        List.of(30L, 1L),
                        List.of(30L, 2L),
                        List.of(40L, 1L),
                        List.of(40L, 2L)
                ),
                repository.addedDependenciesIfAbsent
        );
        assertEquals(TASK_ID, repository.deletedTaskId);
    }

    @Test
    void skipsSelfDependencyWhenReconnectWouldPointTaskToItself() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.owner);
        repository.dependencyIds = List.of(30L);
        repository.dependentIds = List.of(30L);
        repository.deleted = true;
        TaskService service = service(repository);

        service.delete(TASK_ID, USER_ID);

        assertEquals(List.of(), repository.addedDependenciesIfAbsent);
        assertEquals(TASK_ID, repository.deletedTaskId);
    }

    @Test
    void addsDependencyWhenUserCanEditTask() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.editor);
        TaskService service = service(repository);

        service.addDependency(TASK_ID, 30L, USER_ID);

        assertEquals(TASK_ID, repository.dependencyTaskId);
        assertEquals(30L, repository.addedDependencyTaskId);
    }

    @Test
    void rejectsDependencyOnSameTask() {
        TaskService service = service(new RecordingTaskRepository());

        assertThrows(
                TaskSelfDependencyException.class,
                () -> service.addDependency(TASK_ID, TASK_ID, USER_ID)
        );
    }

    @Test
    void rejectsDuplicateDependency() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.owner);
        repository.dependencyExists = true;
        TaskService service = service(repository);

        assertThrows(
                TaskDependencyAlreadyExistsException.class,
                () -> service.addDependency(TASK_ID, 30L, USER_ID)
        );
    }

    @Test
    void rejectsMissingDependencyTask() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.missingTaskId = 30L;
        TaskService service = service(repository);

        assertThrows(
                TaskNotFoundException.class,
                () -> service.addDependency(TASK_ID, 30L, USER_ID)
        );
    }

    @Test
    void rejectsDependencyWhenUserCannotEditTask() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.viewer);
        TaskService service = service(repository);

        assertThrows(
                TaskAccessDeniedException.class,
                () -> service.addDependency(TASK_ID, 30L, USER_ID)
        );
    }

    @Test
    void rejectsDependencyThatCreatesCycle() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.owner);
        repository.dependencyEdges = List.of(
                List.of(30L, 40L),
                List.of(40L, TASK_ID)
        );
        TaskService service = service(repository);

        assertThrows(
                TaskDependencyCycleException.class,
                () -> service.addDependency(TASK_ID, 30L, USER_ID)
        );
    }

    @Test
    void deletesDependencyWhenUserCanEditTask() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.editor);
        repository.dependencyDeleted = true;
        TaskService service = service(repository);

        service.deleteDependency(TASK_ID, 30L, USER_ID);

        assertEquals(TASK_ID, repository.deletedDependencyTaskId);
        assertEquals(30L, repository.deletedDependencyId);
    }

    @Test
    void rejectsDeletingMissingDependency() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.owner);
        TaskService service = service(repository);

        assertThrows(
                TaskDependencyNotFoundException.class,
                () -> service.deleteDependency(TASK_ID, 30L, USER_ID)
        );
    }

    @Test
    void rejectsDeletingDependencyWhenUserCannotEditTask() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.task = Optional.of(task(TASK_ID, USER_ID));
        repository.memberRole = Optional.of(TaskMemberRole.viewer);
        TaskService service = service(repository);

        assertThrows(
                TaskAccessDeniedException.class,
                () -> service.deleteDependency(TASK_ID, 30L, USER_ID)
        );
    }

    private static TaskDto task(Long id, Long createdByUserId) {
        return new TaskDto(id, createdByUserId, "Task", "Description", 1, null, null, null, null);
    }

    private static TaskDto task(
            Long id,
            Long createdByUserId,
            TaskStatus status,
            Integer priority,
            OffsetDateTime deadline
    ) {
        return new TaskDto(id, createdByUserId, "Task", "Description", priority, status, deadline, null, null);
    }

    private static TaskService service(RecordingTaskRepository repository) {
        return new TaskService(repository, new TaskFilter(), new TaskSorter() {
            @Override
            public List<TaskDto> sort(
                    List<TaskDto> tasks,
                    SortAlgorithm algorithm,
                    TaskSortField field,
                    SortDirection direction
            ) {
                return tasks;
            }
        });
    }

    private static TaskUpdateDto update() {
        return new TaskUpdateDto("Task", "Description", 1, null, null, false);
    }

    private static final class RecordingTaskRepository implements TaskRepository {
        private List<TaskDto> tasks = List.of();
        private Optional<TaskDto> task = Optional.empty();
        private Optional<TaskMemberRole> memberRole = Optional.empty();
        private TaskDto createdTask;
        private Optional<TaskDto> updatedTask = Optional.empty();
        private boolean deleted;
        private TaskDto taskToCreate;
        private TaskDto taskToUpdate;
        private Long ownerTaskId;
        private Long ownerUserId;
        private Long deletedTaskId;
        private boolean dependencyExists;
        private List<List<Long>> dependencyEdges = List.of();
        private Long dependencyTaskId;
        private Long addedDependencyTaskId;
        private boolean dependencyDeleted;
        private Long deletedDependencyTaskId;
        private Long deletedDependencyId;
        private Long missingTaskId;
        private List<Long> dependencyIds = List.of();
        private List<Long> dependentIds = List.of();
        private final List<List<Long>> addedDependenciesIfAbsent = new ArrayList<>();

        @Override
        public List<TaskDto> findAllByUserId(Long userId) {
            return tasks;
        }

        @Override
        public Optional<TaskDto> findById(Long taskId) {
            if (taskId.equals(missingTaskId)) {
                return Optional.empty();
            }
            return task;
        }

        @Override
        public Optional<TaskMemberRole> findMemberRole(Long taskId, Long userId) {
            return memberRole;
        }

        @Override
        public TaskDto create(TaskDto task) {
            taskToCreate = task;
            return createdTask;
        }

        @Override
        public void addOwner(Long taskId, Long userId) {
            ownerTaskId = taskId;
            ownerUserId = userId;
        }

        @Override
        public boolean dependencyExists(Long taskId, Long dependencyTaskId) {
            return dependencyExists;
        }

        @Override
        public List<List<Long>> findAllDependencyEdges() {
            return dependencyEdges;
        }

        @Override
        public void addDependency(Long taskId, Long dependencyTaskId) {
            this.dependencyTaskId = taskId;
            this.addedDependencyTaskId = dependencyTaskId;
        }

        @Override
        public boolean deleteDependency(Long taskId, Long dependencyTaskId) {
            deletedDependencyTaskId = taskId;
            deletedDependencyId = dependencyTaskId;
            return dependencyDeleted;
        }

        @Override
        public List<Long> findDependencyIds(Long taskId) {
            return dependencyIds;
        }

        @Override
        public List<Long> findDependentIds(Long taskId) {
            return dependentIds;
        }

        @Override
        public void addDependencyIfAbsent(Long taskId, Long dependencyTaskId) {
            addedDependenciesIfAbsent.add(List.of(taskId, dependencyTaskId));
        }

        @Override
        public Optional<TaskDto> update(TaskDto task) {
            taskToUpdate = task;
            return updatedTask;
        }

        @Override
        public boolean deleteById(Long taskId) {
            deletedTaskId = taskId;
            return deleted;
        }
    }
}
