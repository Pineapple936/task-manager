package com.team.task_manager.service.algorithm.tree;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

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
	private Comparator comparator;

	public BinarySearchTree(Comparator comparator) {
		this.comparator = comparator;
	}

	public insert(T item) {
		poot = insertRecurs(root, item);
	}

	public Node<T> insertRecurs(Node<T> current, T item) {
		if (current == null) {
			return new Node<>(item);
		}

		if (comparator.compare(item, current.data) >= 0) {
			current.right = insertRecurs(current.right, item);
		} else {
			current.left = insertRecurs(current.left, item);
		}

		return current;
	}

	public List<T> getTopN(int n) {
		List<T> result = new ArrayList<>();

		collectTopN(root, result, n);

		return result;
	}

	public collectTopN(Node<T> current, List<T> result, int n) {
		if (current == null || result.size() >= n) {
			return result;
		}

		collectTopN(current.right, result, n);

		if (result.size() < n) {
			result.add(current.data);
		}

		collectTopN(current.left, result, n);
	}
}
