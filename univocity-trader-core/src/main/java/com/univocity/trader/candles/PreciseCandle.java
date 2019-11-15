package com.univocity.trader.candles;

import java.math.*;

public class PreciseCandle {

	public long openTime;
	public long closeTime;
	public BigDecimal open;
	public BigDecimal high;
	public BigDecimal low;
	public BigDecimal close;
	public BigDecimal volume;

	public PreciseCandle() {

	}

	public PreciseCandle(long openTime, long closeTime, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal volume) {
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}

	@Override
	public String toString() {
		return "{" +
				"openTime=" + openTime +
				", closeTime=" + closeTime +
				", open=" + open +
				", high=" + high +
				", low=" + low +
				", close=" + close +
				", volume=" + volume +
				'}';
	}
}
