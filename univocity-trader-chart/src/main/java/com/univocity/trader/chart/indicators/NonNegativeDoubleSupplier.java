package com.univocity.trader.chart.indicators;

import java.util.function.*;

public final class NonNegativeDoubleSupplier implements DoubleSupplier {

	private final DoubleSupplier supplier;

	public NonNegativeDoubleSupplier(DoubleSupplier supplier) {
		this.supplier = supplier;
	}

	public final double getAsDouble() {
		double out = supplier.getAsDouble();
		if (out < 0) {
			return 0.0;
		}
		return out;
	}
}
