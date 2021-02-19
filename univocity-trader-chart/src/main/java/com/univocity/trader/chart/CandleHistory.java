package com.univocity.trader.chart;

import com.univocity.trader.candles.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class CandleHistory implements Iterable<Candle> {

	private final List<Candle> candles = new ArrayList<>(1000);
	private final Set<Consumer<UpdateType>> dataUpdateListeners = ConcurrentHashMap.newKeySet();

	public enum UpdateType {
		NEW_HISTORY, INCREMENT, SELECTION
	}

	public void addDataUpdateListener(Consumer<UpdateType> r) {
		dataUpdateListeners.add(r);
	}

	public void removeDataUpdateListener(Consumer<UpdateType> r) {
		dataUpdateListeners.remove(r);
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
			notifyUpdateListeners(UpdateType.INCREMENT);
		}
	}

	public void addSilently(Candle candle) {
		candles.add(candle);
	}

	public void add(Candle candle) {
		addSilently(candle);
		notifyUpdateListeners(UpdateType.INCREMENT);
	}

	public void updateSilently(Candle candle) {
		candles.set(size() - 1, candle);
	}

	public void update(Candle candle) {
		updateSilently(candle);
		notifyUpdateListeners(UpdateType.INCREMENT);
	}


	public boolean addOrUpdateSilently(Candle candle) {
		Candle last = getLast();

		int comparison = last.compareTo(candle);
		if (comparison < 0) {
			addSilently(candle);
			return true;
		} else if (comparison == 0) {
			updateSilently(candle);
			return true;
		}
		return false;
	}

	public void addOrUpdate(Candle candle) {
		if(addOrUpdateSilently(candle)){
			notifyUpdateListeners(UpdateType.INCREMENT);
		}
	}

	public void setCandles(List<Candle> candles) {
		this.candles.clear();
		this.candles.addAll(candles);
		notifyUpdateListeners(UpdateType.NEW_HISTORY);
	}

	public void notifyUpdateListeners(UpdateType updateType) {
		dataUpdateListeners.forEach(c -> c.accept(updateType));
	}

	public CandleHistoryView newView() {
		return new CandleHistoryView(this);
	}

	public Candle getFirst() {
		return get(0);
	}

	public Candle getLast() {
		return get(size() - 1);
	}

	public Candle getAtTime(long time) {
		if (time < getFirst().openTime) {
			return getFirst();
		} else if (time > getLast().closeTime) {
			return getLast();
		}

		Candle key = new Candle(time, time, 0, 0, 0, 0, 0);
		int position = Collections.binarySearch(candles, key, timeSearch);
		if (position >= 0) {
			return candles.get(position);
		}
		return null;
	}

	private static final Comparator<Candle> timeSearch = (o1, o2) -> {
		if (o1.openTime >= o2.openTime) {
			if (o1.closeTime <= o2.closeTime) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	};
}
