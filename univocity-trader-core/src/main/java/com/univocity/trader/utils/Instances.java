package com.univocity.trader.utils;

public class Instances<T> extends AbstractNewInstances<T, Instances<T>> {

	public Instances(T[] empty) {
		super(empty);
	}

	public Instances<T> add(T reusableInstance) {
		add((s, p) -> reusableInstance);
		return this;
	}
}
