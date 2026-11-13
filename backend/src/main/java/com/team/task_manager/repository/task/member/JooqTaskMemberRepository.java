package com.team.task_manager.repository.task.member;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.repository.task.entity.TaskMemberDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.team.task_manager.jooq.generated.Tables.TASK_MEMBERS;
import static com.team.task_manager.jooq.generated.Tables.USERS;

@Repository
@RequiredArgsConstructor
public class JooqTaskMemberRepository implements TaskMemberRepository {
    private final DSLContext dsl;

    @Override
    public Optional<TaskMemberRole> findRole(Long taskId, Long userId) {
        return dsl.select(TASK_MEMBERS.ROLE)
                .from(TASK_MEMBERS)
                .where(TASK_MEMBERS.TASK_ID.eq(taskId))
                .and(TASK_MEMBERS.USER_ID.eq(userId))
                .fetchOptional(TASK_MEMBERS.ROLE);
    }

    @Override
    public List<TaskMemberDto> findAll(Long taskId) {
        return dsl.select(
                        TASK_MEMBERS.TASK_ID,
                        TASK_MEMBERS.USER_ID,
                        USERS.USERNAME,
                        TASK_MEMBERS.ROLE
                )
                .from(TASK_MEMBERS)
                .join(USERS)
                .on(USERS.ID.eq(TASK_MEMBERS.USER_ID))
                .where(TASK_MEMBERS.TASK_ID.eq(taskId))
                .orderBy(TASK_MEMBERS.ADDED_AT, TASK_MEMBERS.USER_ID)
                .fetch(record -> new TaskMemberDto(
                        record.get(TASK_MEMBERS.TASK_ID),
                        record.get(TASK_MEMBERS.USER_ID),
                        record.get(USERS.USERNAME),
                        record.get(TASK_MEMBERS.ROLE)
                ));
    }

    @Override
    public void add(Long taskId, Long userId, TaskMemberRole role) {
        dsl.insertInto(TASK_MEMBERS)
                .set(TASK_MEMBERS.TASK_ID, taskId)
                .set(TASK_MEMBERS.USER_ID, userId)
                .set(TASK_MEMBERS.ROLE, role)
                .execute();
    }

    @Override
    public void addOwners(List<Long> taskIds, Long userId) {
        if (taskIds.isEmpty()) {
            return;
        }
        var insert = dsl.insertInto(TASK_MEMBERS,
                TASK_MEMBERS.TASK_ID, TASK_MEMBERS.USER_ID, TASK_MEMBERS.ROLE);
        for (Long taskId : taskIds) {
            insert.values(taskId, userId, TaskMemberRole.owner);
        }
        insert.execute();
    }

    @Override
    public boolean updateRole(Long taskId, Long userId, TaskMemberRole role) {
        return dsl.update(TASK_MEMBERS)
                .set(TASK_MEMBERS.ROLE, role)
                .where(TASK_MEMBERS.TASK_ID.eq(taskId))
                .and(TASK_MEMBERS.USER_ID.eq(userId))
                .execute() == 1;
    }

    @Override
    public boolean delete(Long taskId, Long userId) {
        return dsl.deleteFrom(TASK_MEMBERS)
                .where(TASK_MEMBERS.TASK_ID.eq(taskId))
                .and(TASK_MEMBERS.USER_ID.eq(userId))
                .execute() == 1;
    }
}
