package com.team.task_manager.repository.task.member;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.repository.task.entity.TaskMemberDto;

public interface TaskMemberRepository {
    Optional<TaskMemberRole> findRole(Long taskId, Long userId);

    List<TaskMemberDto> findAll(Long taskId);

    void add(Long taskId, Long userId, TaskMemberRole role);

    void addOwners(List<Long> taskIds, Long userId);

    boolean updateRole(Long taskId, Long userId, TaskMemberRole role);

    boolean delete(Long taskId, Long userId);
}
