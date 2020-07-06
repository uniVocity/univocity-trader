package com.univocity.trader.chart.indicators;

import java.util.function.*;

public class ArgumentValue {

	private final String name;
	private final Object value;

	ArgumentValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	ArgumentValue(String name, Object argumentValue, Object editorValue) {
		this.name = name;
		this.value = new Object[]{argumentValue, editorValue};
	}

	public Object getArgumentValue() {
		if (value instanceof Supplier) {
			return ((Supplier<?>) value).get();
		}
		if (value instanceof Object[]) {
			return ((Object[]) value)[0];
		}

		return value;
	}

	public Object getEditorValue() {
		if (!(value instanceof Supplier)) {
			if (value instanceof Object[]) {
				return ((Object[]) value)[1];
			}
			return value;
		}
		return null;
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}


}
