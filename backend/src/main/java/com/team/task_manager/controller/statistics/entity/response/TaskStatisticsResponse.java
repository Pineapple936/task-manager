package com.team.task_manager.controller.statistics.entity.response;

import java.util.Map;

public record TaskStatisticsResponse(
        long total,
        long completed,
        long started,
        long planned,
        Map<Integer, Long> priorityDistribution
) {
}
