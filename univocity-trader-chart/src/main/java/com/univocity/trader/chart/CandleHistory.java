package com.univocity.trader.chart;

import com.univocity.trader.candles.*;

import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class CandleHistory implements Iterable<Candle> {

	private List<Candle> tradeHistory = new ArrayList<>(1000);
	private final List<Runnable> dataUpdateListeners = new ArrayList<>();

	public void addDataUpdateListener(Runnable r) {
		dataUpdateListeners.add(r);
	}

	public int size() {
		return tradeHistory.size();
	}

	public Candle get(int i) {
		if (i < 0 || i > tradeHistory.size() - 1) {
			return null;
		}
		return tradeHistory.get(i);
	}

	@Override
	public Iterator<Candle> iterator() {
		return tradeHistory.iterator();
	}

	public boolean isEmpty() {
		return tradeHistory.isEmpty();
	}

	public int indexOf(Candle candle) {
		if(candle == null){
			return -1;
		}
		return Collections.binarySearch(tradeHistory, candle);
	}

	public void addAll(Collection<Candle> candles) {
		if (this.tradeHistory.addAll(candles)) {
			notifyUpdateListeners();
		}
	}

	public void addSilently(Candle candle) {
		tradeHistory.add(candle);
	}

	public void add(Candle candle) {
		tradeHistory.add(candle);
		notifyUpdateListeners();
	}

	public void notifyUpdateListeners() {
		dataUpdateListeners.forEach(Runnable::run);
	}

	public CandleHistoryView newView(){
		return new CandleHistoryView(this);
	}
}
