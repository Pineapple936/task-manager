package com.team.task_manager.controller.error.exception.sort;

public class NullComparatorException extends RuntimeException {
    public NullComparatorException() {
        super("Comparator cannot be null");
    }
}
