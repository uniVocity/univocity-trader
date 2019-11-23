package com.univocity.trader.simulation;

import java.util.*;

public abstract class Parameters {

	public static final Parameters NULL = new Parameters() {
		@Override
		public String printParameters() {
			return "{}";
		}
	};

	protected abstract String printParameters();

	public final String toString() {
		return printParameters();
	}

	public Parameters fromString(String s){
		throw new UnsupportedOperationException();
	}

	public <T extends Parameters> Collection<T> fromString(Collection<String> parameters) {
		List<T> out = new ArrayList<>();
		for (String p : parameters) {
			out.add((T) this.fromString(p));
		}
		return out;
	}
}
