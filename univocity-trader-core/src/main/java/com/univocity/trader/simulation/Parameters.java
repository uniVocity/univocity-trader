package com.univocity.trader.simulation;

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
}
