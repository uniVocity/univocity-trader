package com.univocity.trader.simulation;

import java.util.*;

public abstract class Parameters {

	public static final Parameters NULL = new Parameters() {
		@Override
		public String printParameters() {
			return "{}";
		}
	};
	private String myString = null;

	protected abstract String printParameters();

	public final String toString() {
		if(myString == null){
			myString = printParameters();
		}
		return myString;
	}

	public Parameters fromString(String s) {
		throw new UnsupportedOperationException();
	}

	public <T extends Parameters> Collection<T> fromString(Collection<String> parameters) {
		List<T> out = new ArrayList<>();
		for (String p : parameters) {
			out.add((T) this.fromString(p));
		}
		return out;
	}

	@Override
	public final int hashCode() {
		return toString().hashCode();
	}

	@Override
	public final boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		return toString().equals(o.toString());
	}
}
