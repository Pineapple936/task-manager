package com.team.task_manager.repository.task;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.jooq.generated.tables.records.TasksRecord;
import com.team.task_manager.repository.task.entity.TaskDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JooqTaskRepository implements TaskRepository {
    private final DSLContext dsl;

    @Override
    public List<TaskDto> findAllByUserId(Long userId) {
        return List.of();
    }

    @Override
    public Optional<TaskDto> findById(Long taskId) {
        return Optional.empty();
    }

    @Override
    public TaskDto create(TaskDto task) {
        return null;
    }

    @Override
    public Optional<TaskDto> update(TaskDto task, Long userId) {
        return Optional.empty();
    }

    @Override
    public boolean deleteByIdAndUserId(Long taskId, Long userId) {
        return false;
    }

    private TaskDto toDto(TasksRecord record) {
        return new TaskDto(
                record.getId(),
                record.getCreatedByUserId(),
                record.getName(),
                record.getDescription(),
                record.getPriority(),
                record.getStatus(),
                record.getDeadline().toLocalDateTime(),
                record.getCreatedAt().toLocalDateTime(),
                record.getUpdatedAt().toLocalDateTime()
        );
    }
}
