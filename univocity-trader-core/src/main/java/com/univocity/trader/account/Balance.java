package com.univocity.trader.account;

import java.math.*;

public class Balance {

	public static final Balance ZERO = new Balance(null);

	private final String symbol;
	private BigDecimal free = BigDecimal.ZERO;
	private BigDecimal locked = BigDecimal.ZERO;
	private double freeAmount = -1.0;

	public static final MathContext ROUND_MC = new MathContext(8, RoundingMode.FLOOR);

	public Balance(String symbol) {
		this.symbol = symbol;
	}

	public Balance(String symbol, double free) {
		this.symbol = symbol;
		this.free = BigDecimal.valueOf(free);
	}

	public String getSymbol() {
		return symbol;
	}

	public BigDecimal getFree() {
		return free;
	}

	public double getFreeAmount() {
		if(freeAmount < 0.0){
			freeAmount = free.doubleValue();
		}
		return freeAmount;
	}

	public void setFree(BigDecimal free) {
		this.free = round(free == null ? BigDecimal.ZERO : free);
		this.freeAmount = -1.0;
	}

	public BigDecimal getLocked() {
		return locked;
	}

	public void setLocked(BigDecimal locked) {
		this.locked = round(locked == null ? BigDecimal.ZERO : locked);
	}

	public BigDecimal getTotal() {
		return round(free.add(locked));
	}

	@Override
	public String toString() {
		return "{" +
				"'" + symbol + '\'' +
				", free=" + free +
				", locked=" + locked +
				'}';
	}

	public static final BigDecimal round(BigDecimal bd){
		return bd.round(ROUND_MC);
	}

	public static final String roundStr(BigDecimal bd){
		return round(bd).toPlainString();
	}

}
