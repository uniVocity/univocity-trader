package com.univocity.trader.utils;

import com.univocity.trader.candles.*;

public class CircularCandleList {
	public final Candle[] candles;
	public int i;
	private Candle last;
	private boolean updating;
	private long count;

	public CircularCandleList(int length) {
		this.candles = new Candle[length];
	}

	public final Candle first() {
		if (updating) {
			return getRecentValue(Math.min(size(), candles.length));
		} else if (size() < candles.length) {
			return candles[0];
		} else {
			return getRecentValue(candles.length);
		}
	}

	public final Candle last() {
		return last;
	}

	public void update(Candle candle) {
		update(candle, true);
	}

	protected void update(Candle candle, boolean updating) {
		candles[i] = candle;
		last = candle;
		this.updating = updating;
	}

	public final void accumulate(Candle candle, boolean updating) {
		if (updating) {
			update(candle, true);
		} else {
			add(candle);
		}
	}

	public void add(Candle candle) {
		update(candle, false);
		count++;
		i = (i + 1) % candles.length;

	}

	public final int size() {
		return Math.min(candles.length, (int) (updating ? count + 1 : count));
	}

	public final Candle get(int i) {
		return candles[i % candles.length];
	}

	public final int capacity() {
		return candles.length;
	}

	public final int getStartingIndex() {
		return getStartingIndex(count < candles.length ? (int) count : candles.length);
	}

	public final int getStartingIndex(int backwardCount) {
		if (backwardCount == 0) {
			throw new IllegalArgumentException("Invalid recent value index");
		}
		backwardCount = i - backwardCount;

		if (backwardCount < 0) {
			backwardCount = candles.length + backwardCount;
			while (backwardCount < 0) {
				backwardCount = candles.length + backwardCount;
			}
		}

		if (updating) {
			backwardCount = (backwardCount + 1) % candles.length;
		}
		return backwardCount;
	}

	public final Candle getRecentValue(int backwardCount) {
		return candles[getStartingIndex(backwardCount)];
	}

	public String toString(){
		StringBuilder out = new StringBuilder("[");
		int start = getStartingIndex();
		int len = size();

		for(int i = 0; i < len; i++){
			if(out.length() > 1){
				out.append("; ");
			}
			out.append(get(i+start));
		}

		out.append(']');
		return out.toString();
	}
}
