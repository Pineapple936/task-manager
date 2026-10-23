package com.team.task_manager.service.statistics.entity;

public record TaskStatisticsDto(
        long planned,
        long started,
        long completed
) {
}
