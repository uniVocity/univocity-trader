package com.univocity.trader.simulation;

import org.springframework.util.*;

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

	@Override
	public IntParameters fromString(String s) {
		s = StringUtils.deleteAny(s, " []{}()");
		if(s.isBlank()){
			return new IntParameters();
		}
		String[] params = s.split(",");
		int[] p = new int[params.length];
		for (int i = 0; i < params.length; i++) {
			p[i] = Integer.parseInt(params[i]);
		}

		return new IntParameters(p);
	}
}