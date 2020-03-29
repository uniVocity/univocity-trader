package com.univocity.trader.utils;

public class CircularList {
	public final double[] values;
	public int i;
	private double sum;
	private double last;
	private boolean updating;
	private long count;

	public CircularList(int length) {
		this.values = new double[length];
	}

	public final double first() {
		if (updating) {
			return getRecentValue(Math.min(size(), values.length));
		} else if (size() < values.length) {
			return values[0];
		} else {
			return getRecentValue(values.length);
		}
	}

	public final double last() {
		return last;
	}

	public void update(double value) {
		update(value, true);
	}

	protected void update(double value, boolean updating) {
		sum -= values[i];
		sum += value;
		values[i] = value;
		last = value;
		this.updating = updating;
	}

	public final void accumulate(double value, boolean updating) {
		if (updating) {
			update(value, true);
		} else {
			add(value);
		}
	}

	public void add(double value) {
		update(value, false);
		count++;
		i = (i + 1) % values.length;

	}

	public final int size() {
		return Math.min(values.length, (int) (updating ? count + 1 : count));
	}

	public final double get(int i) {
		return values[i % values.length];
	}

	public final int capacity() {
		return values.length;
	}

	public final double sum() {
		return sum;
	}

	public final int getStartingIndex(int backwardCount) {
		if (backwardCount == 0) {
			throw new IllegalArgumentException("Invalid recent value index");
		}
		backwardCount = i - backwardCount;

		if (backwardCount < 0) {
			backwardCount = values.length + backwardCount;
			while (backwardCount < 0) {
				backwardCount = values.length + backwardCount;
			}
		}

		if (updating) {
			backwardCount = (backwardCount + 1) % values.length;
		}
		return backwardCount;
	}

	public final double avg() {
		return sum / size();
	}

	public interface DoublePredicate {
		boolean test(double d1, double d2);
	}

	private double calculateOnSection(int backwardCount, DoublePredicate predicate) {
		final int len = size();
		int start = i - backwardCount;
		if (start < 0) {
			start = 0;
		}
		double out = 0.0;
		for (int i = start; i < len && backwardCount > 0; i++, backwardCount--) {
			double v = values[i];
			if (v != 0.0 && (out == 0.0 || predicate.test(out, v))) {
				out = v;
			}
		}
		for (int i = len; i >= 0 && backwardCount > 0; i--, backwardCount--) {
			double v = values[i];
			if (v != 0.0 && (out == 0.0 || predicate.test(out, v))) {
				out = v;
			}
		}

		return out;
	}

	public final double getMin(int backwardCount) {
		return calculateOnSection(backwardCount, (min, v) -> v < min);
	}

	public final double getMax(int backwardCount) {
		return calculateOnSection(backwardCount, (max, v) -> v > max);
	}

	public final double getRecentValue(int backwardCount) {
		return values[getStartingIndex(backwardCount)];
	}
}
