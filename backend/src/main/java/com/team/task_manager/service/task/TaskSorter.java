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

import com.team.task_manager.controller.error.exception.sort.EmptyTaskListException;
import com.team.task_manager.controller.error.exception.sort.UnsupportedSortAlgorithmException;

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
        if (tasks == null) {
            throw new EmptyTaskListException();
        }
        if (tasks.isEmpty()) {
            return tasks;
        }

        Comparator<TaskDto> comparator = createComparator(field, direction);

        switch (algorithm) {
            case QUICK:
                return quickSorter.sort(tasks, comparator);
            case MERGE:
                return mergeSorter.sort(tasks, comparator);
            case INSERTION:
                return insertionSorter.sort(tasks, comparator);
            default:
                throw new UnsupportedSortAlgorithmException(algorithm.toString());
        }
    }

    private Comparator<TaskDto> createComparator(TaskSortField field, SortDirection direction) {
        return switch (field) {
            case NAME -> {
                Comparator<String> nameOrder = String.CASE_INSENSITIVE_ORDER;
                if (direction == SortDirection.DESC) {
                    nameOrder = nameOrder.reversed();
                }
                
                yield Comparator.comparing(TaskDto::name, Comparator.nullsLast(nameOrder));
            }
            case PRIORITY -> {
                Comparator<Integer> priorityOrder = (direction == SortDirection.DESC)
                        ? Comparator.reverseOrder()
                        : Comparator.naturalOrder();
                

                Comparator<java.time.OffsetDateTime> deadlineOrder = (direction == SortDirection.DESC)
                        ? Comparator.naturalOrder()
                        : Comparator.reverseOrder();
                
                yield Comparator.comparing(TaskDto::priority, Comparator.nullsLast(priorityOrder))
                        .thenComparing(TaskDto::deadline, Comparator.nullsLast(deadlineOrder))
                        .thenComparing(TaskDto::id, Comparator.nullsLast(Comparator.naturalOrder));
            }
            case DEADLINE -> {
                Comparator<java.time.OffsetDateTime> deadlineOrder = (direction == SortDirection.DESC)
                        ? Comparator.reverseOrder()
                        : Comparator.naturalOrder();
                
                yield Comparator.comparing(TaskDto::deadline, Comparator.nullsLast(deadlineOrder));
            }
            default -> throw new IllegalArgumentException("Неизвестное поле: " + field);
        };
    }
}
