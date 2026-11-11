package com.team.task_manager.comtroller.task.entity.response

import java.util.List;

public record TaskGraphResponse (
    List<TaskListItemResponse> nodes
) {

}
    