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
		if(i < values.length) {
			values[i] = valueSupplier.getAsDouble();
		}
	}

	@Override
	public final void nextValue() {
		if(i < values.length) {
			values[i++] = valueSupplier.getAsDouble();
		}
	}

	@Override
	public final void paintNext(int i, BasicChart<?> chart, Graphics2D g, int width) {
		if(i < values.length) {
			paintNext(i, values[i], chart, g, width);
		}
	}

	@Override
	public final T getTheme(){
		return theme;
	}

	protected abstract void paintNext(int i, double value, BasicChart<?> chart, Graphics2D g, int width);
}
