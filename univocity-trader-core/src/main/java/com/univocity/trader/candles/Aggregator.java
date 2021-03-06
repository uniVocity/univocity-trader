package com.univocity.trader.candles;

import com.univocity.trader.indicators.base.*;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public final class Aggregator {

	protected final Map<Long, SoftReference<Aggregator>> allInstances;
	protected final String description;

	protected final long ms;
	protected final long minutes;

	protected Candle full;
	protected Candle partial;

	public Aggregator(String description) {
		this(new ConcurrentHashMap<>(), description, TimeInterval.millis(0));
	}

	protected Aggregator(Map<Long, SoftReference<Aggregator>> allInstances, String description, TimeInterval time) {
		this.minutes = time.ms / MINUTE.ms;
		this.ms = time.ms % MINUTE.ms;
		this.allInstances = allInstances;
		this.description = description + "-" + time;
		if (time.ms > 0) {
			if (!allInstances.containsKey(time.ms)) {
				allInstances.put(time.ms, new SoftReference<>(this));
			} //else forget this instance, won't be used.
		}
	}

	public Aggregator getInstance(TimeInterval time) {
		SoftReference<Aggregator> instance = allInstances.get(time.ms);
		if (instance == null) {
			return new Aggregator(allInstances, description, time);
		}
		return instance.get();
	}

	public Aggregator[] getAggregators() {
		return allInstances.values().stream().map(SoftReference::get).toArray(Aggregator[]::new);
	}

	public void aggregate(Candle candle) {
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

	public void setFull(Candle candle){
		this.full = candle;
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
