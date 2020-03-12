package com.univocity.trader.account;

import java.util.*;

public class OrderSet {

	public int i;
	public Order[] elements = new Order[16];

	public final void addOrReplace(Order order) {
		int slot = Arrays.binarySearch(elements, 0, i, order);
		if (slot >= 0) {
			elements[slot] = order;
		} else {
			add(order);
		}
	}

	public final void add(Order order) {
		if (i >= elements.length) {
			elements = Arrays.copyOf(elements, elements.length + 16);
		}
		elements[i++] = order;
		sort();
	}

	public final Order get(Order order) {
		int slot = Arrays.binarySearch(elements, 0, i, order);
		if (slot >= 0) {
			return elements[slot];
		}
		return null;
	}

	public final boolean contains(Order order) {
		if (order == null) {
			return false;
		}
		int slot = Arrays.binarySearch(elements, 0, i, order);
		return slot >= 0;
	}

	public final void remove(Order order) {
		int slot = Arrays.binarySearch(elements, 0, i, order);
		if (slot >= 0) {
			System.arraycopy(elements, slot + 1, elements, slot, i - slot - 1);
			i--;
			sort();
		}
	}

	private void sort() {
		Arrays.sort(elements, 0, i);
	}

	public final int size() {
		return i;
	}

	public final void clear() {
		i = 0;
	}

	public final boolean isEmpty() {
		return i == 0;
	}

	public final void removeNulls() {
		int n = 0;
		for (int i = 0; i < this.i; i++) {
			if (elements[i] != null) {
				elements[n++] = elements[i];
			}
		}
		i = n;
	}

	public final Collection<Order> asList() {
		ArrayList<Order> out = new ArrayList<>(i);
		for (int i = 0; i < this.i; i++) {
			out.add(elements[i]);
		}
		return out;
	}

	public final void addAll(OrderSet position) {
		if (position.i == 0) {
			return;
		}
		if (this.i + position.i > this.elements.length) {
			this.elements = Arrays.copyOf(this.elements, this.i + position.i);
		}
		System.arraycopy(position.elements, 0, this.elements, i, position.i);
		this.i += position.i;
	}
}
