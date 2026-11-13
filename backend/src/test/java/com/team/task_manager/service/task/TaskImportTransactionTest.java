package com.team.task_manager.service.task;

import java.util.List;
import java.util.UUID;

import com.team.task_manager.controller.task.entity.TaskMapper;
import com.team.task_manager.controller.task.entity.request.CreateTaskRequest;
import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.jooq.generated.enums.TaskStatus;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import static com.team.task_manager.jooq.generated.Tables.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskImportTransactionTest {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private DSLContext dsl;
    @MockitoSpyBean
    private TaskMemberService taskMemberService;
    private Long userId;

    @BeforeEach
    void createUser() {
        userId = dsl.insertInto(USERS).set(USERS.USERNAME, "import-test-" + UUID.randomUUID())
                .set(USERS.PASSWORD_HASH, "test-hash").returning(USERS.ID).fetchSingle(USERS.ID);
    }

    @AfterEach
    void cleanUp() {
        if (userId != null) {
            dsl.deleteFrom(TASKS).where(TASKS.CREATED_BY_USER_ID.eq(userId)).execute();
            dsl.deleteFrom(USERS).where(USERS.ID.eq(userId)).execute();
        }
    }

    @Test
    void savesAllTasksAndTheirOwners() {
        var first = taskMapper.toCreateDto(new CreateTaskRequest("Первая", null, 0, null));
        var second = taskMapper.toCreateDto(new CreateTaskRequest("Вторая", null, 10, null));
        taskService.createAll(List.of(first, second), userId);

        var tasks = dsl.selectFrom(TASKS).where(TASKS.CREATED_BY_USER_ID.eq(userId)).fetch();
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().allMatch(task -> task.getStatus() == TaskStatus.planned));
        assertEquals(2, dsl.fetchCount(TASK_MEMBERS,
                TASK_MEMBERS.USER_ID.eq(userId).and(TASK_MEMBERS.ROLE.eq(TaskMemberRole.owner))));
    }

    @Test
    void rejectsEntireTaskInsertWhenOnePriorityIsInvalid() {
        var valid = taskMapper.toCreateDto(new CreateTaskRequest("Первая", null, 1, null));
        var invalid = taskMapper.toCreateDto(new CreateTaskRequest("Вторая", null, 11, null));

        assertThrows(DataIntegrityViolationException.class,
                () -> taskService.createAll(List.of(valid, invalid), userId));

        assertEquals(0, dsl.fetchCount(TASKS, TASKS.CREATED_BY_USER_ID.eq(userId)));
        assertEquals(0, dsl.fetchCount(TASK_MEMBERS, TASK_MEMBERS.USER_ID.eq(userId)));
    }
    @Test
    void rollsBackTasksWhenAddingOwnersFails() {
        var first = taskMapper.toCreateDto(new CreateTaskRequest("Первая", null, 1, null));
        var second = taskMapper.toCreateDto(new CreateTaskRequest("Вторая", null, 2, null));
        doAnswer(invocation -> {
            assertEquals(2, dsl.fetchCount(TASKS, TASKS.CREATED_BY_USER_ID.eq(userId)));
            throw new IllegalStateException("Ошибка добавления владельцев");
        }).when(taskMemberService).addOwners(anyList(), eq(userId));

        assertThrows(IllegalStateException.class,
                () -> taskService.createAll(List.of(first, second), userId));

        assertEquals(0, dsl.fetchCount(TASKS, TASKS.CREATED_BY_USER_ID.eq(userId)));
        assertEquals(0, dsl.fetchCount(TASK_MEMBERS, TASK_MEMBERS.USER_ID.eq(userId)));
    }

}
