package com.team.task_manager.service.algorithm.sort;

import java.util.Comparator;
import java.util.List;

public interface Sorter<T> {
    List<T> sort(List<T> items, Comparator<T> comparator);
}
