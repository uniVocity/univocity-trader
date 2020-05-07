package com.univocity.trader.account;

import java.util.*;

public class SortedSet<T extends Comparable<T>> {

	public int i;
	public T[] elements;

	private final Comparator<T> comparator = Comparator.reverseOrder();

	protected SortedSet(T[] initialStorage) {
		this.elements = initialStorage;
	}

	public final void addOrReplace(T order) {
		if (i == 0) {
			add(order);
		} else {
			int slot = Arrays.binarySearch(elements, 0, i, order, comparator);
			if (slot >= 0) {
				elements[slot] = order;
			} else {
				add(order);
			}
		}
	}

	public boolean replace(T order) {
		if(i == 1 && elements[0].compareTo(order) == 0){
			elements[0] = order;
			return true;
		}
		int slot = Arrays.binarySearch(elements, 0, i, order, comparator);
		if (slot >= 0) {
			elements[slot] = order;
			return true;
		}
		return false;
	}

	public synchronized final void add(T order) {
		if (i == 0) {
			elements[i++] = order;
		} else {
			int slot = Arrays.binarySearch(elements, 0, i, order, comparator);
			if (slot >= 0) {
				elements[slot] = order;
				return;
			}

			if (i >= elements.length) {
				elements = Arrays.copyOf(elements, elements.length + elements.length);
			}
			elements[i++] = order;
			sort();
		}
	}

	public final T get(T order) {
		int slot = Arrays.binarySearch(elements, 0, i, order, comparator);
		if (slot >= 0) {
			return (T) elements[slot];
		}
		return null;
	}

	public final boolean contains(T order) {
		if (order == null) {
			return false;
		}
		int slot = Arrays.binarySearch(elements, 0, i, order, comparator);
		return slot >= 0;
	}

	public final void remove(T order) {
		if (i != 0) {
			if (i == 1) {
				if (order.compareTo(elements[0]) == 0) {
					elements[0] = null;
					i = 0;
				}
			} else {
				int slot = Arrays.binarySearch(elements, 0, i, order, comparator);
				if (slot >= 0) {
					elements[slot] = null;
					removeNulls();
				}
			}
		}
	}

	private void sort() {
		removeNulls();
		if(i > 1) {
			Arrays.sort(elements, 0, i, comparator);
		}
	}

	public final int size() {
		return i;
	}

	public final void clear() {
		Arrays.fill(elements, 0, i, null);
		i = 0;
	}

	public final boolean isEmpty() {
		return i == 0;
	}

	void removeNulls() {
		int n = 0;
		for (int i = 0; i < this.i; i++) {
			if (elements[i] != null) {
				elements[n++] = elements[i];
			}
		}
		i = n;
	}

	public final Collection<T> asList() {
		ArrayList<T> out = new ArrayList<>(i);
		for (int i = this.i - 1; i >= 0; i--) {
			out.add(elements[i]);
		}
		return out;
	}

	public final Set<T> asSet() {
		Set<T> out = new TreeSet<>();
		for (int i = 0; i < this.i; i++) {
			out.add((T) elements[i]);
		}
		return out;
	}


	public final void addAll(SortedSet<T> position) {
		if (position.i == 0) {
			return;
		}
		if (this.i + position.i > this.elements.length) {
			this.elements = Arrays.copyOf(this.elements, this.i + position.i);
		}
		System.arraycopy(position.elements, 0, this.elements, i, position.i);
		this.i += position.i;
		sort();
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (int i = this.i - 1; i >= 0; i--) {
			if (elements[i] == null) {
				break;
			}
			if (out.length() > 0) {
				out.append(',');
			}
			out.append(elements[i]);
		}

		return out.toString();
	}
}
