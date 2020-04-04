package com.univocity.trader.chart;

import com.univocity.trader.candles.*;

import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class CandleHistory implements Iterable<Candle> {

	private List<Candle> candles = new ArrayList<>(1000);
	private final List<Runnable> dataUpdateListeners = new ArrayList<>();

	public void addDataUpdateListener(Runnable r) {
		dataUpdateListeners.add(r);
	}

	public int size() {
		return candles.size();
	}

	public Candle get(int i) {
		if (i < 0 || i > candles.size() - 1) {
			return null;
		}
		return candles.get(i);
	}

	@Override
	public Iterator<Candle> iterator() {
		return candles.iterator();
	}

	public boolean isEmpty() {
		return candles.isEmpty();
	}

	public int indexOf(Candle candle) {
		if (candle == null) {
			return -1;
		}
		return Collections.binarySearch(candles, candle);
	}

	public void addAll(Collection<Candle> candles) {
		if (this.candles.addAll(candles)) {
			notifyUpdateListeners();
		}
	}

	public void addSilently(Candle candle) {
		candles.add(candle);
	}

	public void add(Candle candle) {
		candles.add(candle);
		notifyUpdateListeners();
	}

	public void setCandles(List<Candle> candles){
		this.candles.clear();
		this.candles.addAll(candles);
		notifyUpdateListeners();
	}

	public void notifyUpdateListeners() {
		dataUpdateListeners.forEach(Runnable::run);
	}

	public CandleHistoryView newView() {
		return new CandleHistoryView(this);
	}

	public Candle getFirst(){
		return get(0);
	}

	public Candle getLast() {
		return get(size() - 1);
	}
}
