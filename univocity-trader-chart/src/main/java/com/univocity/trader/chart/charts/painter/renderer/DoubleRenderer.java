package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;
import java.util.function.*;

public abstract class DoubleRenderer<T extends Theme> implements Renderer<T> {

	private double[] values;
	private int i = 0;
	private final DoubleSupplier valueSupplier;
	protected final T theme;

	public DoubleRenderer(T theme, DoubleSupplier valueSupplier) {
		this.valueSupplier = valueSupplier;
		this.theme = theme;
	}

	@Override
	public final void reset(int length) {
		values = new double[length];
		i = 0;
	}

	@Override
	public final void updateValue() {
		if (i < values.length) {
			values[i] = valueSupplier.getAsDouble();
		}
	}

	@Override
	public final void nextValue() {
		if (i < values.length) {
			values[i++] = valueSupplier.getAsDouble();
		}
	}

	@Override
	public final void paintNext(int i, BasicChart<?> chart, Graphics2D g, int width) {
		if (i < values.length) {
			paintNext(i, values[i], chart, g, width);
		}
	}

	@Override
	public final T getTheme() {
		return theme;
	}

	protected abstract void paintNext(int i, double value, BasicChart<?> chart, Graphics2D g, int width);

	@Override
	public double getMaximumValue(int from, int to) {
		to = Math.max(to, values.length);
		if (from < to) {
			double max = values[from];
			for (int i = from; i < to; i++) {
				max = Math.max(values[i], max);
			}
			return max;
		}
		return Integer.MIN_VALUE;
	}

	@Override
	public double getMinimumValue(int from, int to) {
		to = Math.max(to, values.length);
		if (from < to) {
			double min = values[from];
			for (int i = from; i < to; i++) {
				min = Math.min(values[i], min);
			}
			return min;
		}
		return Integer.MAX_VALUE;
	}
}
