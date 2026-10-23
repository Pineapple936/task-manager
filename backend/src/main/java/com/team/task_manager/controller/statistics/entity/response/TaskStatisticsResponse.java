package com.team.task_manager.controller.statistics.entity.response;

public record TaskStatisticsResponse(
        long planned,
        long started,
        long completed
) {
}
