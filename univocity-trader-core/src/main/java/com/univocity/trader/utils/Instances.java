package com.univocity.trader.utils;

public  final class Instances<T> extends AbstractNewInstances<T, Instances<T>> {

	public Instances(T[] empty) {
		super(empty);
	}

	public final Instances<T> add(T reusableInstance) {
		add((s, p) -> reusableInstance);
		return this;
	}
}
