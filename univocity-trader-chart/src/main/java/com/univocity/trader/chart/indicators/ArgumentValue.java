package com.univocity.trader.chart.indicators;

import java.util.function.*;

public class ArgumentValue {

	private final String name;
	private final Object argumentValue;
	private final Object editorValue;

	ArgumentValue(String name, Object value) {
		this.name = name;
		this.argumentValue = value;
		this.editorValue = value;
	}

	ArgumentValue(String name, Object argumentValue, Object editorValue) {
		this.name = name;
		this.argumentValue = argumentValue;
		this.editorValue = editorValue;
	}

	public Object getArgumentValue() {
		if (argumentValue instanceof Supplier) {
			return ((Supplier<?>) argumentValue).get();
		}
		return argumentValue;
	}

	public Object getEditorValue() {
		return editorValue;
	}

	@Override
	public String toString() {
		return name + "=" + editorValue;
	}


}
