package com.team.task_manager.service.statistics.entity;

import java.util.Map;

public record TaskStatisticsDto(
        long total,
        long completed,
        long started,
        long planned,
        Map<Integer, Long> priorityDistribution
) {
}
