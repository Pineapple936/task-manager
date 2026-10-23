package com.team.task_manager.controller.error.exception.task;

public class TaskSelfDependencyException extends RuntimeException {

    public TaskSelfDependencyException() {
        super("Task cannot depend on itself");
    }
}
