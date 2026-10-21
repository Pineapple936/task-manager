package com.team.task_manager.repository.task;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.jooq.generated.tables.records.TasksRecord;
import com.team.task_manager.repository.task.entity.TaskDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.team.task_manager.jooq.generated.Tables.TASKS;
import static com.team.task_manager.jooq.generated.Tables.TASK_MEMBERS;
import static org.jooq.impl.DSL.currentOffsetDateTime;
import static org.jooq.impl.DSL.exists;

@Repository
@RequiredArgsConstructor
public class JooqTaskRepository implements TaskRepository {
    private final DSLContext dsl;

    @Override
    public List<TaskDto> findAllByUserId(Long userId) {
        return dsl
                .select(TASKS.fields())
                .from(TASKS)
                .join(TASK_MEMBERS)
                .on(TASK_MEMBERS.TASK_ID.eq(TASKS.ID))
                .where(TASK_MEMBERS.USER_ID.eq(userId))
                .fetch(record -> toDto(record.into(TASKS)));
    }

    @Override
    public Optional<TaskDto> findByIdAndUserId(Long taskId, Long userId) {
        return dsl
                .select(TASKS.fields())
                .from(TASKS)
                .join(TASK_MEMBERS)
                .on(TASK_MEMBERS.TASK_ID.eq(TASKS.ID))
                .where(TASKS.ID.eq(taskId))
                .and(TASK_MEMBERS.USER_ID.eq(userId))
                .fetchOptional(record -> toDto(record.into(TASKS)));
    }

    @Override
    public TaskDto create(TaskDto task) {
        TasksRecord createdTask = dsl.insertInto(TASKS)
                .set(TASKS.CREATED_BY_USER_ID, task.createdByUserId())
                .set(TASKS.NAME, task.name())
                .set(TASKS.DESCRIPTION, task.description())
                .set(TASKS.PRIORITY, task.priority())
                .set(TASKS.STATUS, task.status())
                .set(TASKS.DEADLINE, task.deadline())
                .returning()
                .fetchSingle();

        return toDto(createdTask);
    }

    @Override
    public Optional<TaskDto> update(TaskDto task, Long userId) {
        return Optional.ofNullable(
                        dsl.update(TASKS)
                                .set(TASKS.NAME, task.name())
                                .set(TASKS.DESCRIPTION, task.description())
                                .set(TASKS.PRIORITY, task.priority())
                                .set(TASKS.STATUS, task.status())
                                .set(TASKS.DEADLINE, task.deadline())
                                .set(TASKS.UPDATED_AT, currentOffsetDateTime())
                                .where(TASKS.ID.eq(task.id()))
                                .and(exists(
                                        dsl.selectOne()
                                                .from(TASK_MEMBERS)
                                                .where(TASK_MEMBERS.TASK_ID.eq(TASKS.ID))
                                                .and(TASK_MEMBERS.USER_ID.eq(userId))
                                                .and(TASK_MEMBERS.ROLE.in(TaskMemberRole.owner, TaskMemberRole.editor))
                                ))
                                .returning()
                                .fetchOne()
                )
                .map(JooqTaskRepository::toDto);
    }

    @Override
    public boolean deleteByIdAndUserId(Long taskId, Long userId) {
        return dsl.deleteFrom(TASKS)
                .where(TASKS.ID.eq(taskId))
                .and(exists(
                        dsl.selectOne()
                                .from(TASK_MEMBERS)
                                .where(TASK_MEMBERS.TASK_ID.eq(TASKS.ID))
                                .and(TASK_MEMBERS.USER_ID.eq(userId))
                                .and(TASK_MEMBERS.ROLE.eq(TaskMemberRole.owner))
                ))
                .execute() == 1;
    }

    private static TaskDto toDto(TasksRecord record) {
        return new TaskDto(
                record.getId(),
                record.getCreatedByUserId(),
                record.getName(),
                record.getDescription(),
                record.getPriority(),
                record.getStatus(),
                record.getDeadline(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
