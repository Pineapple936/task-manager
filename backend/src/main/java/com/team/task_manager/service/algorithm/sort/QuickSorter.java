package com.team.task_manager.service.algorithm.sort;

import com.team.task_manager.controller.error.exception.sort.EmptyTaskListException;
import com.team.task_manager.controller.error.exception.sort.NullComparatorException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class QuickSorter<T> implements Sorter<T> {

    @Override
    public List<T> sort(List<T> items, Comparator<T> comparator) {
        if (items == null) {
            throw new EmptyTaskListException();
        }
        if (comparator == null) {
            throw new NullComparatorException();
        }
        
        // Результат сортировки
        List<T> result = new ArrayList<>(items);

        quickSort(result, 0, result.size() - 1, comparator);

        return result;
    }

    private void quickSort(List<T> list, int left, int right, Comparator<T> comparator) {
        while (left < right) {
            
            int pivotIndex = ThreadLocalRandom.current().nextInt(left, right + 1);
            swap(list, pivotIndex, right);
            T pivot = list.get(right);

            int store = left;
            for (int i = left; i < right; i ++) {
                if (comparator.compare(list.get(i), pivot) < 0) {
                    swap(list, i, store);
                    store ++;  
                }
            }
            swap(list, store, right);

            if (store - left < right - store) {
                quickSort(list, left, store - 1, comparator);
                left = store + 1;
            } else {
                quickSort(list, store + 1, right, comparator);
                right = store - 1;
            }
        }
    }

    // Смена значений переменных
    private void swap(List<T> list, int i, int j) {
        if (i == j) {
            return;
        }
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
