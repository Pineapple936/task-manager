package com.team.task_manager.service.algorithm.tree;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

public class BinarySearchTree<T> {

	private static class Node<T> {
		T data;
		Node<T> right;
		Node<T> left;

		 Node(T data) {
			this.data = data;
		}
	}

	private Node<T> root;
	private Comparator<T> comparator;

	public BinarySearchTree(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	public void insert(T item) {
		root = insertRecurs(root, item);
	}

	public Node<T> insertRecurs(Node<T> current, T item) {
		if (current == null) {
			return new Node<>(item);
		}

		if (comparator.compare(item, current.data) > 0) {
			current.right = insertRecurs(current.right, item);
		} else if (comparator.compare(item, current.data) < 0) {
			current.left = insertRecurs(current.left, item);
		} else {
			if (ThreadLocalRandom.current().nextBoolean()) {
				current.right = insertRecurs(current.right, item);
			} else {
				current.left = insertRecurs(current.left, item);
			}
		}

		return current;
	}

	public List<T> getTopN(int n) {
		List<T> result = new ArrayList<>();

		collectTopN(root, result, n);

		return result;
	}

	public void collectTopN(Node<T> current, List<T> result, int n) {
		if (current == null || result.size() >= n) {
			return;
		}

		collectTopN(current.right, result, n);

		if (result.size() < n) {
			result.add(current.data);
		}

		collectTopN(current.left, result, n);
	}
}
