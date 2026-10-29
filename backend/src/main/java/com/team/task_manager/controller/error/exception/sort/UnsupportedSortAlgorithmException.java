package com.team.task_manager.controller.error.exception.sort;

public class UnsupportedSortAlgorithmException extends RuntimeException {
    public UnsupportedSortAlgorithmException(String algorithm) {
        super("Unsupported sort algorithm: " + algorithm);
    }
}
