package com.univocity.trader.chart.indicators;

import java.util.function.*;

public class ArgumentValue {

	private final String name;
	private Object value;

	ArgumentValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public Object getValue() {
		if(value instanceof Supplier){
			return ((Supplier<?>) value).get();
		}
		return value;
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}


}
