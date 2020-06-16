package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;
import java.util.function.*;

public abstract class DoubleRenderer<T extends Theme> implements Renderer<T> {

	private double[] values = new double[0];
	private int i = 0;
	private final DoubleSupplier valueSupplier;
	protected final T theme;
	private final String description;

	public DoubleRenderer(String description, T theme, DoubleSupplier valueSupplier) {
		this.valueSupplier = valueSupplier;
		this.theme = theme;
		this.description = description;
	}

	@Override
	public String description() {
		return description;
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
	public final void paintNext(int i, BasicChart<?> chart, Graphics2D g, AreaPainter painter) {
		if (i < values.length) {
			paintNext(i, values[i], chart, g, painter);
		}
	}

	@Override
	public final T getTheme() {
		return theme;
	}

	public void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		if (i < values.length) {
			updateSelection(i, values[i], candle, candleLocation, chart, g, painter, headerLine);
		}
	}

	protected void updateSelection(int i, double value, Candle candle, Point candleLocation, BasicChart<?> chart, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		String string = SymbolPriceDetails.toString(8, value);
		if (headerLine.length() > 0 && headerLine.charAt(headerLine.length() - 1) != '[') {
			headerLine.append(", ");
		}
		String description = description();
		if (description != null && !description.isBlank()) {
			headerLine.append(description());
			headerLine.append(':');
		}
		headerLine.append(string);
	}

	protected abstract void paintNext(int i, double value, BasicChart<?> chart, Graphics2D g, AreaPainter painter);

	@Override
	public double getMaximumValue(int from, int to) {
		to = Math.max(to, values.length);
		if (from < to) {
			double max = values[from];
			for (int i = from; i < to && i < values.length; i++) {
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
			for (int i = from; i < to && i < values.length; i++) {
				min = Math.min(values[i], min);
			}
			return min;
		}
		return Integer.MAX_VALUE;
	}
}
