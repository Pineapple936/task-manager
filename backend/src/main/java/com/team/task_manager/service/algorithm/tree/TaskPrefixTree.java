package com.team.task_manager.service.algorithm.tree;

import com.team.task_manager.repository.task.entity.TaskDto;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

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

        String name = task.name().toLowerCase();
        Node current = root;

        for (char ch : name.toCharArray()) {
            current.children.putIfAbsent(ch, new Node());
            current = current.children.get(ch);
        }

        current.isEndOfWord = true;
        current.tasks.add(task);
    }

    public List<TaskDto> search(String name) {
        if (name == null) return List.of();

        Node node = findNode(name.toLowerCase());

        if (node != null && node.isEndOfWord) {
            return node.tasks;
        }
        return List.of();
    }

    public List<TaskDto> searchByPrefix(String prefix) {
        if (prefix == null) return List.of();

        Node node = findNode(prefix.toLowerCase());
        if (node == null) return List.of();

        List<TaskDto> result = new ArrayList<>();
        collectAllTasks(node, result);

        return result;
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

    private void collectAllTasks(Node node, List<TaskDto> result) {
        if (node == null) return;

        if (node.isEndOfWord) {
            result.addAll(node.tasks);
        }

        for (Node child : node.children.values()) {
            collectAllTasks(child, result);
        }
    }
}