package com.team.task_manager.service.task;

import java.util.Comparator;
import java.util.List;

import com.team.task_manager.service.algorithm.sort.InsertionSorter;
import com.team.task_manager.service.algorithm.sort.MergeSorter;
import com.team.task_manager.service.algorithm.sort.QuickSorter;
import com.team.task_manager.service.algorithm.sort.Sorter;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.service.task.entity.sort.SortAlgorithm;
import com.team.task_manager.service.task.entity.sort.SortDirection;
import com.team.task_manager.service.task.entity.sort.TaskSortField;
import org.springframework.stereotype.Service;

@Service
public class TaskSorter {
    private final Sorter<TaskDto> quickSorter = new QuickSorter<>();
    private final Sorter<TaskDto> insertionSorter = new InsertionSorter<>();
    private final Sorter<TaskDto> mergeSorter = new MergeSorter<>();

    public List<TaskDto> sort(
            List<TaskDto> tasks,
            SortAlgorithm algorithm,
            TaskSortField field,
            SortDirection direction
    ) {
        return List.of();
    }

    private Comparator<TaskDto> createComparator(TaskSortField field, SortDirection direction) {
        return null;
    }
}
