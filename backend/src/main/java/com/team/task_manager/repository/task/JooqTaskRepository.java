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
    public Optional<TaskDto> findById(Long taskId) {
        return dsl
                .select(TASKS.fields())
                .from(TASKS)
                .where(TASKS.ID.eq(taskId))
                .fetchOptional(record -> toDto(record.into(TASKS)));
    }

    @Override
    public Optional<TaskMemberRole> findMemberRole(Long taskId, Long userId) {
        return dsl
                .select(TASK_MEMBERS.ROLE)
                .from(TASK_MEMBERS)
                .where(TASK_MEMBERS.TASK_ID.eq(taskId))
                .and(TASK_MEMBERS.USER_ID.eq(userId))
                .fetchOptional(TASK_MEMBERS.ROLE);
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
    public void addOwner(Long taskId, Long userId) {
        dsl.insertInto(TASK_MEMBERS)
                .set(TASK_MEMBERS.TASK_ID, taskId)
                .set(TASK_MEMBERS.USER_ID, userId)
                .set(TASK_MEMBERS.ROLE, TaskMemberRole.owner)
                .execute();
    }

    @Override
    public Optional<TaskDto> update(TaskDto task) {
        return Optional.ofNullable(
                        dsl.update(TASKS)
                                .set(TASKS.NAME, task.name())
                                .set(TASKS.DESCRIPTION, task.description())
                                .set(TASKS.PRIORITY, task.priority())
                                .set(TASKS.STATUS, task.status())
                                .set(TASKS.DEADLINE, task.deadline())
                                .set(TASKS.UPDATED_AT, currentOffsetDateTime())
                                .where(TASKS.ID.eq(task.id()))
                                .returning()
                                .fetchOne()
                )
                .map(JooqTaskRepository::toDto);
    }

    @Override
    public boolean deleteById(Long taskId) {
        return dsl.deleteFrom(TASKS)
                .where(TASKS.ID.eq(taskId))
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
