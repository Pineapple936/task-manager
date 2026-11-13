package com.team.task_manager.repository.task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.member.JooqTaskMemberRepository;
import com.team.task_manager.repository.user.UserRepository;
import com.team.task_manager.service.tag.TagService;
import com.team.task_manager.service.task.TaskFilter;
import com.team.task_manager.service.task.TaskMemberService;
import com.team.task_manager.service.task.TaskService;
import com.team.task_manager.service.task.TaskSorter;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import static com.team.task_manager.jooq.generated.Tables.TASKS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JooqTaskImportTest {
    @Test
    void usesTwoStatementsForDifferentTaskCounts() {
        for (int size : new int[]{1, 3, 1000}) {
            List<String> statements = new ArrayList<>();
            List<Object[]> bindings = new ArrayList<>();
            var resultDsl = DSL.using(SQLDialect.POSTGRES);
            var ids = resultDsl.newResult(TASKS.ID);
            for (int index = 0; index < size; index++) {
                ids.add(resultDsl.newRecord(TASKS.ID).values(100L + index));
            }
            var connection = new MockConnection(context -> {
                statements.add(context.sql());
                bindings.add(context.bindings());
                return statements.size() == 1
                        ? new MockResult[]{new MockResult(size, ids)}
                        : new MockResult[]{new MockResult(size)};
            });
            var tasks = IntStream.range(0, size)
                    .mapToObj(index -> new TaskDto(null, 999L, "Task " + index, "", 1,
                            TaskStatus.planned, null, null, null)).toList();

            service(connection).createAll(tasks, 10L);

            assertEquals(2, statements.size(), "task count=" + size);
            assertTrue(statements.getFirst().contains("insert into \"public\".\"tasks\""));
            assertTrue(statements.getFirst().contains("returning"));
            assertTrue(statements.getLast().contains("insert into \"public\".\"task_members\""));
            assertEquals(size * 6, bindings.getFirst().length);
            assertEquals(size * 3, bindings.getLast().length);
            for (int index = 0; index < size; index++) {
                assertEquals("10", String.valueOf(bindings.getFirst()[index * 6]));
                assertEquals(String.valueOf(100L + index), String.valueOf(bindings.getLast()[index * 3]));
                assertEquals("10", String.valueOf(bindings.getLast()[index * 3 + 1]));
                assertEquals("owner", String.valueOf(bindings.getLast()[index * 3 + 2]));
            }
        }
    }

    @Test
    void emptyListDoesNotExecuteSql() {
        var connection = new MockConnection(context -> {
            fail("Empty import must not execute SQL");
            return new MockResult[0];
        });
        service(connection).createAll(List.of(), 10L);
        var dsl = DSL.using(connection, SQLDialect.POSTGRES);
        assertTrue(new JooqTaskRepository(dsl).createAll(List.of(), 10L).isEmpty());
        new JooqTaskMemberRepository(dsl).addOwners(List.of(), 10L);
    }

    private TaskService service(MockConnection connection) {
        var dsl = DSL.using(connection, SQLDialect.POSTGRES);
        var taskRepository = new JooqTaskRepository(dsl);
        var memberService = new TaskMemberService(taskRepository,
                new JooqTaskMemberRepository(dsl), mock(UserRepository.class));
        return new TaskService(taskRepository, mock(TagService.class), memberService,
                new TaskFilter(), new TaskSorter());
    }
}
