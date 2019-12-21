package com.univocity.trader.utils;

public class NewInstances<T> extends AbstractNewInstances<T, NewInstances<T>> {

	public NewInstances(T[] empty) {
		super(empty);
	}
}
