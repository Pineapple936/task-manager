package com.team.task_manager.repository.task;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.team.task_manager.repository.tag.entity.TagDto;
import com.team.task_manager.jooq.generated.tables.records.TasksRecord;
import com.team.task_manager.repository.task.entity.TaskDependencyDto;
import com.team.task_manager.repository.task.entity.TaskDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.team.task_manager.jooq.generated.Tables.TASKS;
import static com.team.task_manager.jooq.generated.Tables.TAGS;
import static com.team.task_manager.jooq.generated.Tables.TASK_DEPENDENCIES;
import static com.team.task_manager.jooq.generated.Tables.TASK_MEMBERS;
import static com.team.task_manager.jooq.generated.Tables.TASK_TAGS;
import static org.jooq.impl.DSL.currentOffsetDateTime;

@Repository
@RequiredArgsConstructor
public class JooqTaskRepository implements TaskRepository {
    private final DSLContext dsl;

    @Override
    public List<TaskDto> findAllByUserId(Long userId) {
        List<TaskDto> tasks = dsl
                .select(TASKS.fields())
                .from(TASKS)
                .join(TASK_MEMBERS)
                .on(TASK_MEMBERS.TASK_ID.eq(TASKS.ID))
                .where(TASK_MEMBERS.USER_ID.eq(userId))
                .fetch(record -> toDto(record.into(TASKS)));

        return withRelations(tasks);
    }

    @Override
    public Optional<TaskDto> findById(Long taskId) {
        return dsl
                .select(TASKS.fields())
                .from(TASKS)
                .where(TASKS.ID.eq(taskId))
                .fetchOptional(record -> toDto(record.into(TASKS)))
                .map(task -> withRelations(List.of(task)).getFirst());
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
    public List<Long> createAll(List<TaskDto> tasks, Long userId) {
        if (tasks.isEmpty()) {
            return List.of();
        }
        var insert = dsl.insertInto(TASKS,
                TASKS.CREATED_BY_USER_ID, TASKS.NAME, TASKS.DESCRIPTION,
                TASKS.PRIORITY, TASKS.STATUS, TASKS.DEADLINE);
        for (TaskDto task : tasks) {
            insert.values(userId, task.name(), task.description(), task.priority(), task.status(), task.deadline());
        }
        return insert.returningResult(TASKS.ID).fetch(TASKS.ID);
    }

    @Override
    public boolean dependencyExists(Long taskId, Long dependencyTaskId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(TASK_DEPENDENCIES)
                        .where(TASK_DEPENDENCIES.TASK_ID.eq(taskId))
                        .and(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID.eq(dependencyTaskId))
        );
    }

    @Override
    public List<List<Long>> findAllDependencyEdges() {
        return dsl.select(
                        TASK_DEPENDENCIES.TASK_ID,
                        TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID
                )
                .from(TASK_DEPENDENCIES)
                .fetch(record -> List.of(
                        record.get(TASK_DEPENDENCIES.TASK_ID),
                        record.get(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID)
                ));
    }

    @Override
    public void addDependency(Long taskId, Long dependencyTaskId) {
        dsl.insertInto(TASK_DEPENDENCIES)
                .set(TASK_DEPENDENCIES.TASK_ID, taskId)
                .set(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID, dependencyTaskId)
                .execute();
    }

    @Override
    public boolean deleteDependency(Long taskId, Long dependencyTaskId) {
        return dsl.deleteFrom(TASK_DEPENDENCIES)
                .where(TASK_DEPENDENCIES.TASK_ID.eq(taskId))
                .and(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID.eq(dependencyTaskId))
                .execute() == 1;
    }

    @Override
    public List<Long> findDependencyIds(Long taskId) {
        return dsl.select(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID)
                .from(TASK_DEPENDENCIES)
                .where(TASK_DEPENDENCIES.TASK_ID.eq(taskId))
                .fetch(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID);
    }

    @Override
    public List<Long> findDependentIds(Long taskId) {
        return dsl.select(TASK_DEPENDENCIES.TASK_ID)
                .from(TASK_DEPENDENCIES)
                .where(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID.eq(taskId))
                .fetch(TASK_DEPENDENCIES.TASK_ID);
    }

    @Override
    public void addDependencyIfAbsent(Long taskId, Long dependencyTaskId) {
        dsl.insertInto(TASK_DEPENDENCIES)
                .set(TASK_DEPENDENCIES.TASK_ID, taskId)
                .set(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID, dependencyTaskId)
                .onConflictDoNothing()
                .execute();
    }

    @Override
    public boolean taskTagExists(Long taskId, Long tagId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(TASK_TAGS)
                        .where(TASK_TAGS.TASK_ID.eq(taskId))
                        .and(TASK_TAGS.TAG_ID.eq(tagId))
        );
    }

    @Override
    public void addTag(Long taskId, Long tagId) {
        dsl.insertInto(TASK_TAGS)
                .set(TASK_TAGS.TASK_ID, taskId)
                .set(TASK_TAGS.TAG_ID, tagId)
                .execute();
    }

    @Override
    public void deleteTag(Long taskId, Long tagId) {
        dsl.deleteFrom(TASK_TAGS)
                .where(TASK_TAGS.TASK_ID.eq(taskId))
                .and(TASK_TAGS.TAG_ID.eq(tagId))
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
                .map(JooqTaskRepository::toDto)
                .map(updatedTask -> updatedTask
                        .withDependencies(task.dependencies())
                        .withTags(task.tags()));
    }

    @Override
    public void deleteById(Long taskId) {
        dsl.deleteFrom(TASKS)
                .where(TASKS.ID.eq(taskId))
                .execute();
    }

    private List<TaskDto> withRelations(List<TaskDto> tasks) {
        return withTags(withDependencies(tasks));
    }

    private List<TaskDto> withDependencies(List<TaskDto> tasks) {
        if (tasks.isEmpty()) {
            return tasks;
        }

        var dependencyTask = TASKS.as("dependency_task");
        Map<Long, List<TaskDependencyDto>> dependenciesByTaskId = dsl
                .select(
                        TASK_DEPENDENCIES.TASK_ID,
                        dependencyTask.ID,
                        dependencyTask.NAME,
                        dependencyTask.STATUS
                )
                .from(TASK_DEPENDENCIES)
                .join(dependencyTask)
                .on(dependencyTask.ID.eq(TASK_DEPENDENCIES.DEPENDS_ON_TASK_ID))
                .where(TASK_DEPENDENCIES.TASK_ID.in(tasks.stream().map(TaskDto::id).toList()))
                .orderBy(TASK_DEPENDENCIES.TASK_ID, dependencyTask.ID)
                .fetchGroups(
                        TASK_DEPENDENCIES.TASK_ID,
                        record -> new TaskDependencyDto(
                                record.get(dependencyTask.ID),
                                record.get(dependencyTask.NAME),
                                record.get(dependencyTask.STATUS)
                        )
                );

        return tasks.stream()
                .map(task -> task.withDependencies(
                        dependenciesByTaskId.getOrDefault(task.id(), List.of())
                ))
                .toList();
    }

    private List<TaskDto> withTags(List<TaskDto> tasks) {
        if (tasks.isEmpty()) {
            return tasks;
        }

        Map<Long, List<TagDto>> tagsByTaskId = dsl
                .select(TASK_TAGS.TASK_ID, TAGS.ID, TAGS.NAME)
                .from(TASK_TAGS)
                .join(TAGS)
                .on(TAGS.ID.eq(TASK_TAGS.TAG_ID))
                .where(TASK_TAGS.TASK_ID.in(tasks.stream().map(TaskDto::id).toList()))
                .orderBy(TASK_TAGS.TASK_ID, TAGS.ID)
                .fetchGroups(
                        TASK_TAGS.TASK_ID,
                        record -> new TagDto(record.get(TAGS.ID), record.get(TAGS.NAME))
                );

        return tasks.stream()
                .map(task -> task.withTags(tagsByTaskId.getOrDefault(task.id(), List.of())))
                .toList();
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
