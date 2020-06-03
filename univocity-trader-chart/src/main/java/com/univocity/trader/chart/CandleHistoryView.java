package com.univocity.trader.chart;

import com.univocity.trader.candles.*;

import java.util.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class CandleHistoryView implements Iterable<Candle> {

	private Candle from;
	private Candle to;

	private int start;
	private int end;
	private boolean atEnd;

	private final CandleHistory history;

	CandleHistoryView(CandleHistory parent) {
		this.history = parent;
	}

	public void updateView(Candle from, Candle to) {
		boolean changed = false;
		if (this.from != from) {
			start = history.indexOf(from);
			this.from = from;
			changed = true;
		}
		if (this.to != to) {
			end = history.indexOf(to) + 1;
			this.to = to;
			changed = true;
		}

		if (!changed) {
			return;
		}

		if (start == -1) {
			start = 0;
		}
		if (end == -1) {
			end = history.size();
		}
		atEnd = end == history.size();
		notifyUpdateListeners(CandleHistory.UpdateType.SELECTION);
	}

	public void addDataUpdateListener(Consumer<CandleHistory.UpdateType> l) {
		history.addDataUpdateListener(l);
	}

	public void removeDataUpdateListener(Consumer<CandleHistory.UpdateType> l) {
		history.removeDataUpdateListener(l);
	}

	public int size() {
		if(start > end){
			return 0;
		}
		return end - start;
	}

	public Candle get(int i) {
		if (i < 0 || i > size() - 1) {
			return null;
		}
		return history.get(start + i);
	}

	@Override
	public Iterator<Candle> iterator() {
		return new Iterator<>() {
			int i = start;

			@Override
			public boolean hasNext() {
				return i < end;
			}

			@Override
			public Candle next() {
				return history.get(i++);
			}
		};
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public int indexOf(Candle candle) {
		int index = history.indexOf(candle);
		if (index != -1) {
			index -= start;
			if (index < 0 || index > end) {
				return -1;
			}
		}
		return index;
	}

	public void addAll(Collection<Candle> candles) {
		if (atEnd) {
			this.end = history.size() + candles.size();
		}
		history.addAll(candles);
	}

	public void addSilently(Candle candle) {
		if (atEnd) {
			this.end = history.size();
		}
		history.addSilently(candle);
	}

	public void add(Candle candle) {
		if (atEnd) {
			this.end = history.size();
		}
		history.add(candle);
	}

	public void notifyUpdateListeners(CandleHistory.UpdateType updateType) {
		history.notifyUpdateListeners(updateType);
	}

	public Candle getFirst() {
		return history.get(start);
	}

	public Candle getLast() {
		return history.get(start + size() - 1);
	}
}
