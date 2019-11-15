package com.univocity.trader.simulation;

import java.util.*;

public class IntParameters extends Parameters {

	public int[] params;

	public IntParameters(int... params) {
		this.params = params;
	}

	@Override
	protected String printParameters() {
		return Arrays.toString(params);
	}
}