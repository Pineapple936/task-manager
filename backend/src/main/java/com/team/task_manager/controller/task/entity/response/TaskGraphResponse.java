package com.team.task_manager.controller.task.entity.response;

import java.util.List;

public record TaskGraphResponse (
    List<TaskListItemResponse> nodes
) {

}
    