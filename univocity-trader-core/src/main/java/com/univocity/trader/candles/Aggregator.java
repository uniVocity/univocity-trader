package com.univocity.trader.candles;

import com.univocity.trader.indicators.base.*;

import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public class Aggregator {

	private final Map<Long, Aggregator> allInstances;
	private final String description;

	private final long ms;
	private final long minutes;

	private Candle full;
	private Candle partial;

	public Aggregator(String description) {
		this(new ConcurrentHashMap<>(), description, TimeInterval.millis(0));
	}

	private Aggregator(Map<Long, Aggregator> allInstances, String description, TimeInterval time) {
		this.minutes = time.ms / MINUTE.ms;
		this.ms = time.ms % MINUTE.ms;
		this.allInstances = allInstances;
		this.description = description + "-" + time;
		if (time.ms > 0) {
			if (!allInstances.containsKey(time.ms)) {
				allInstances.put(time.ms, this);
			} //else forget this instance, won't be used.
		}
	}

	public static Aggregator getInstance(Aggregator parent, TimeInterval time) {
		Aggregator instance = parent.allInstances.get(time.ms);
		if (instance == null) {
			return new Aggregator(parent.allInstances, parent.description, time);
		}
		return instance;
	}

	public Aggregator[] getAggregators() {
		return allInstances.values().toArray(new Aggregator[0]);
	}

	public void aggregate(Candle candle) {
//		if (skipAggregation) {
//			full = candle;
//			return;
//		}

		if (partial == null) {
			partial = candle;
			full = null;
		} else if (candle.openTime < partial.openTime) {
			return;
		}

		long elapsed = (candle.closeTime - partial.openTime) / (MINUTE.ms - 1L);
		if (elapsed < minutes) {
			partial = partial.merge(candle);
		} else if (elapsed == minutes) {
			if (ms > 1L) {
				elapsed = candle.closeTime - partial.openTime;
				if (elapsed < ms) {
					partial = partial.merge(candle);
				} else {
					full = partial.merge(candle);
					partial = null;
				}
			} else {
				full = partial.merge(candle);
				partial = null;
			}
		} else {
			full = candle;
			partial = null;
		}
	}

	public Candle getFull() {
		return full;
	}

	public Candle getPartial() {
		return partial;
	}

	public String toString() {
		return description;
	}
}
