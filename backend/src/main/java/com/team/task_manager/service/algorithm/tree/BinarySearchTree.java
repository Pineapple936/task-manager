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
		int height;

		 Node(T data) {
			this.data = data;
			this.height = 1;
		}
	}

	private Node<T> root;
	private Comparator<T> comparator;

	public BinarySearchTree(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	private int height(Node<T> node) {
		return (node == null) ? 0 : node.height;
	}

	private void updateHeight(Node<T> node) {
		node.height = 1 + Math.max(height(node.left), height(node.right));
	}

	private int getBalance(Node<T> node) {
		return (node == null) ? 0 : height(node.left) - height(node.right);
	}

	public void insert(T item) {
		root = insertRecurs(root, item);
	}

	private Node<T> insertRecurs(Node<T> current, T item) {
		if (current == null) {
			return new Node<>(item);
		}

		if (comparator.compare(item, current.data) >= 0) {
			current.right = insertRecurs(current.right, item);
		} else {
			current.left = insertRecurs(current.left, item);
		}

		updateHeight(current);

		int balance = getBalance(current);

		if (balance > 1 && comparator.compare(item, current.left.data) < 0) {
			return rightRotate(current);
		}

		if (balance < -1 && comparator.compare(item, current.right.data) >= 0) {
			return leftRotate(current);
		}

		if (balance > 1 && comparator.compare(item, current.left.data) >= 0) {
			current.left = leftRotate(current.left);
			return rightRotate(current);
		}

		if (balance < -1 && comparator.compare(item, current.right.data) < 0) {
			current.right = rightRotate(current.right);
			return leftRotate(current);
		}

		return current;
	}

	public List<T> getTopN(int n) {
		List<T> result = new ArrayList<>();

		collectTopN(root, result, n);

		return result;
	}

	private void collectTopN(Node<T> current, List<T> result, int n) {
		if (current == null || result.size() >= n) {
			return;
		}

		collectTopN(current.right, result, n);

		if (result.size() < n) {
			result.add(current.data);
		}

		collectTopN(current.left, result, n);
	}

	private Node<T> rightRotate(Node<T> a) {
		Node<T> b = a.left;
		Node<T> bR = b.right;

		b.right = a;
		a.left = bR;

		updateHeight(a);
		updateHeight(b);

		return b;
	}

	private Node<T> leftRotate(Node<T> a) {
		Node<T> b = a.right;
		Node<T> bL = b.left;

		b.left = a;
		a.right = bL;

		updateHeight(a);
		updateHeight(b);

		return b;
	}
}
