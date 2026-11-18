package com.team.task_manager.service.algorithm.tree;

import com.team.task_manager.repository.task.entity.TaskDto;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TaskPrefixTree {
    private static class Node {
        boolean isEndOfWord = false;
        List<TaskDto> tasks = new ArrayList<>();

        Map<Character, Node> children = new HashMap<>();
    }

    private Node root;

    public TaskPrefixTree() {
        this.root = new Node();
    }

    public void insert(TaskDto task) {
        if (task == null || task.name() == null) return;

        String[] words = task.name().toLowerCase().split("\\s+");

        for (String word : words) {
            if (word.isBlank()) continue;

            Node current = root;
            
            for (char ch : word.toCharArray()) {
                current.children.putIfAbsent(ch, new Node());
                current = current.children.get(ch);
            }
    
            current.isEndOfWord = true;
            if (!current.tasks.contains(task)) {
                current.tasks.add(task);
            }
        }
    }

    public List<TaskDto> search(String query) {
        if (query == null || query.isBlank()) return List.of();

        String[] words = query.trim().toLowerCase().split("\\s+");

        Map<Long, TaskDto> intersection = null;

        for (String word : words) {
            if (word.isBlank()) continue;

            Node node = findNode(word);
            if (node == null) return List.of();
            
            Map<Long, TaskDto> wordTasks = new LinkedHashMap<>();
            collectAllTasks(node, wordTasks);
    
            if (intersection == null) {
                intersection = wordTasks;
            } else {
                intersection.keySet().retainAll(wordTasks.keySet());
            }
    
            if (intersection.isEmpty()) return List.of();
        }

        if (intersection == null) return List.of();

        return new ArrayList<>(intersection.values());
    }

    private Node findNode(String name) {
        Node current = root;
        
        for (char ch : name.toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    private void collectAllTasks(Node node, Map<Long, TaskDto> result) {
        if (node == null) return;

        if (node.isEndOfWord) {
            for (TaskDto task : node.tasks) {
                result.putIfAbsent(task.id(), task);
            }
        }

        for (Node child : node.children.values()) {
            collectAllTasks(child, result);
        }
    }
}