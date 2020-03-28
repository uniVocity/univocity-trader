package com.univocity.trader.account;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static com.univocity.trader.config.Allocation.*;

public final class Balance implements Cloneable {

	public static final Balance ZERO = new Balance(null, "");

	private final Map<String, AtomicLong> balanceUpdateCounts;
	private final String symbol;
	private double free = 0.0;
	private double locked = 0.0;
	private double shorted = 0.0;
	private Map<String, Double> marginReserves = new ConcurrentHashMap<>();
	private String[] shortedAssetSymbols;
	private boolean tradingLocked = false;

	public static final MathContext ROUND_MC_STR = new MathContext(8, RoundingMode.HALF_EVEN);
	public static final MathContext ROUND_MC = new MathContext(12, RoundingMode.HALF_EVEN);

	public Balance(AccountManager accountManager, String symbol) {
		this.balanceUpdateCounts = accountManager != null ? accountManager.balanceUpdateCounts : new ConcurrentHashMap<>();
		this.symbol = symbol;
	}

	public Balance(AccountManager accountManager, String symbol, double free) {
		this.balanceUpdateCounts = accountManager != null ? accountManager.balanceUpdateCounts : new ConcurrentHashMap<>();
		this.symbol = symbol;
		this.free = ensurePositive(free, "free balance");
	}

	public String getSymbol() {
		return symbol;
	}

	public double getFree() {
		return free;
	}

	public void setFree(double free) {
		this.free = ensurePositive(free, "free balance");
	}

	public double getLocked() {
		return locked;
	}

	public void setLocked(double locked) {
		this.locked = ensurePositive(locked, "locked balance");
	}

	public double getShorted() {
		return shorted;
	}

	public void setShorted(double shorted) {
		this.shorted = ensurePositive(shorted, "shorted balance");
	}

	public double getMarginReserve(String assetSymbol) {
		return marginReserves.getOrDefault(assetSymbol, 0.0);
	}

	public String[] getShortedAssetSymbols() {
		if (shortedAssetSymbols == null) {
			shortedAssetSymbols = marginReserves.keySet().toArray(new String[0]);
		}
		return shortedAssetSymbols;
	}

	public void setMarginReserve(String assetSymbol, double marginReserve) {
		marginReserve = ensurePositive(marginReserve, "margin reserve");
		if (marginReserve <= 0) {
			this.marginReserves.remove(assetSymbol);
		} else {
			this.marginReserves.put(assetSymbol, marginReserve);
		}
		shortedAssetSymbols = null;
	}

	private double ensurePositive(double bd, String field) {
//		String msg = symbol + " " + field + " = " + roundStr(bd);
//		System.out.println(msg);

		//bd = round(bd);
		if (bd >= 0) {
			balanceUpdateCounts.computeIfAbsent(symbol, (s) -> new AtomicLong(1)).incrementAndGet();
			return bd;
		}
		if (bd >= -EFFECTIVELY_ZERO) {
			balanceUpdateCounts.computeIfAbsent(symbol, (s) -> new AtomicLong(1)).incrementAndGet();
			return 0.0;
		} else {
			throw new IllegalStateException(symbol + ": can't set " + field + " to  " + bd);
		}
	}

	public double getTotal() {
		return free + locked;
	}

	@Override
	public String toString() {
		return "{" +
				"'" + symbol + '\'' +
				", free=" + getFree() +
				", locked=" + getLocked() +
				", shorted=" + getShorted() +
				", margin reserves=" + marginReserves +
				'}';
	}

	private static final BigDecimal round(BigDecimal bd, MathContext mc) {
		if (bd.scale() != mc.getPrecision()) {
			return bd.setScale(mc.getPrecision(), mc.getRoundingMode());
		}
		return bd;
	}

	public static final String roundStr(double bd) {
		return round(BigDecimal.valueOf(bd), ROUND_MC_STR).toPlainString();
	}

	public static final double round(double bd) {
		return round(BigDecimal.valueOf(bd), ROUND_MC).doubleValue();
	}

	public final synchronized boolean isTradingLocked() {
		return tradingLocked;
	}

	public final synchronized void lockTrading() {
		tradingLocked = true;
	}

	public final synchronized void unlockTrading() {
		tradingLocked = false;
	}

	public Balance clone() {
		try {
			return (Balance) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

}
