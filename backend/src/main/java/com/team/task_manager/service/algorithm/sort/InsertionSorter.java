package com.team.task_manager.service.algorithm.sort;

import java.util.Comparator;
import java.util.List;

public class InsertionSorter<T> implements Sorter<T> {

    @Override
    public List<T> sort(List<T> items, Comparator<T> comparator) {
        if (items == null) {
            throw new IllegalArgumentException("Список задач не может быть пустым.");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("Компаратор не может быть пустым.");
        }

        // Результат сортировки
        List<T> result = new ArrayList<>(items);

        // Сортировка
        for (int i = 1; i < result.size(); i ++) {
            int j = i - 1;
            T key = result.get(i);

            while (j >= 0 && comparator.compare(result.get(j), key) > 0) {
                result.set(j + 1, result.get(j));
                j --;
            }

            result.set(j + 1, key);
        }
        
        return result;
    }
}
