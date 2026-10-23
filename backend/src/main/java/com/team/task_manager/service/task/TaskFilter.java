package com.team.task_manager.service.task;

import java.util.List;

import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.service.task.entity.filter.TaskFilterCriteria;
import org.springframework.stereotype.Component;

@Component
public class TaskFilter {

    public List<TaskDto> filter(List<TaskDto> tasks, TaskFilterCriteria criteria) {
        return tasks.stream()
                .filter(task -> criteria.status() == null || task.status() == criteria.status())
                .filter(task -> criteria.priority() == null || task.priority().equals(criteria.priority()))
                .filter(task -> criteria.deadlineFrom() == null
                        || task.deadline() != null && !task.deadline().isBefore(criteria.deadlineFrom()))
                .filter(task -> criteria.deadlineTo() == null
                        || task.deadline() != null && !task.deadline().isAfter(criteria.deadlineTo()))
                .toList();
    }
}
