package com.team.task_manager.repository.task.member;

import java.sql.SQLException;
import java.util.List;

import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.repository.task.entity.TaskMemberDto;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import static com.team.task_manager.jooq.generated.Tables.TASK_MEMBERS;
import static com.team.task_manager.jooq.generated.Tables.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JooqTaskMemberRepositoryTest {

    @Test
    void returnsTaskMembersWithUsernamesAndRoles() {
        DSLContext resultDsl = DSL.using(SQLDialect.POSTGRES);
        var result = resultDsl.newResult(
                TASK_MEMBERS.TASK_ID,
                TASK_MEMBERS.USER_ID,
                USERS.USERNAME,
                TASK_MEMBERS.ROLE
        );
        var owner = resultDsl.newRecord(
                TASK_MEMBERS.TASK_ID,
                TASK_MEMBERS.USER_ID,
                USERS.USERNAME,
                TASK_MEMBERS.ROLE
        );
        owner.values(20L, 10L, "egor", TaskMemberRole.owner);
        result.add(owner);

        JooqTaskMemberRepository repository = repository(
                context -> new MockResult[]{new MockResult(1, result)}
        );

        assertEquals(
                List.of(new TaskMemberDto(20L, 10L, "egor", TaskMemberRole.owner)),
                repository.findAll(20L)
        );
    }

    @Test
    void addsMemberWithRequestedRole() {
        RecordingProvider provider = new RecordingProvider();
        JooqTaskMemberRepository repository = repository(provider);

        repository.add(20L, 30L, TaskMemberRole.editor);

        assertTrue(provider.sql.contains("\"task_members\""));
        assertEquals("20", String.valueOf(provider.bindings[0]));
        assertEquals("30", String.valueOf(provider.bindings[1]));
        assertEquals("editor", String.valueOf(provider.bindings[2]));
    }

    @Test
    void reportsRoleUpdateAndMemberDeletion() {
        JooqTaskMemberRepository repository = repository(
                context -> new MockResult[]{new MockResult(1)}
        );

        assertTrue(repository.updateRole(20L, 30L, TaskMemberRole.viewer));
        assertTrue(repository.delete(20L, 30L));
    }

    private static JooqTaskMemberRepository repository(MockDataProvider provider) {
        return new JooqTaskMemberRepository(
                DSL.using(new MockConnection(provider), SQLDialect.POSTGRES)
        );
    }

    private static final class RecordingProvider implements MockDataProvider {
        private String sql;
        private Object[] bindings;

        @Override
        public MockResult[] execute(MockExecuteContext context) throws SQLException {
            sql = context.sql();
            bindings = context.bindings();
            return new MockResult[]{new MockResult(1)};
        }
    }
}
