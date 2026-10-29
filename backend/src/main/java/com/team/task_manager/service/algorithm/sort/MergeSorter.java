package com.team.task_manager.service.algorithm.sort;

import com.team.task_manager.controller.error.exception.sort.EmptyTaskListException;
import com.team.task_manager.controller.error.exception.sort.NullComparatorException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MergeSorter<T> implements Sorter<T> {

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

        List<T> buffer = new ArrayList<>(items);
        mergeSort(result, 0, result.size() - 1, comparator, buffer);
        
        return result;
    }

    // Алгоритм сортировки слиянием
    private void mergeSort(List<T> list, int left, int right, Comparator<T> comparator, List<T> buffer) {
        if (left >= right) {
            return;
        }

        int middle = left + (right - left) / 2;

        // Сортировка половин массива
        mergeSort(list, left, middle, comparator, buffer);
        mergeSort(list, middle + 1, right, comparator, buffer);

        // Слияние отсортированных половинок
        merge(list, left, middle, right, comparator, buffer);
    }

    // Слияние
    private void merge(List<T> list, int left, int middle, int right, Comparator<T> comparator, List<T> buffer) {
        // Заполнение буфера
        for (int i = left; i <= right; i ++) {
            buffer.set(i, list.get(i));
        }

        int i = left;
        int j = middle + 1;
        int k = left;

        // Цикл слияния
        while (i <= middle && j <= right) {
            if (comparator.compare(buffer.get(i), buffer.get(j)) <= 0) {
                list.set(k++, buffer.get(i++));
            } else {
                list.set(k++, buffer.get(j++));
            }
        }

        // Дозаполнение массива
        while (i <= middle) {
            list.set(k++, buffer.get(i++));
        }
        while (j <= right) {
            list.set(k++, buffer.get(j++));
        }
    }


}
