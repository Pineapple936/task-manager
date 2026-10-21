package com.team.task_manager.repository.task.exception;

public class TaskCreationException extends RuntimeException {

    public TaskCreationException() {
        super("Не удалось создать задачу");
    }
}
