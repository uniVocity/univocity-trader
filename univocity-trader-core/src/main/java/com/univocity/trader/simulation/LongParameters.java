package com.univocity.trader.simulation;


import org.apache.commons.lang3.*;

import java.util.*;

public class LongParameters extends Parameters {

	public long[] params;

	public LongParameters(long... params) {
		this.params = params;
	}

	@Override
	protected String printParameters() {
		return Arrays.toString(params);
	}

	@Override
	public LongParameters fromString(String s) {
		if (s.indexOf('[') >= 0) {
			s = StringUtils.substringBetween(s, "[", "]");
		} else if (s.indexOf('(') >= 0) {
			s = StringUtils.substringBetween(s, "(", ")");
		} else if (s.indexOf('{') >= 0) {
			s = StringUtils.substringBetween(s, "{", "}");
		}

		if (s.isBlank()) {
			return new LongParameters();
		}
		String[] params = s.split(",");
		long[] p = new long[params.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = params[i].trim();
			p[i] = Long.parseLong(params[i]);
		}

		return new LongParameters(p);
	}


}