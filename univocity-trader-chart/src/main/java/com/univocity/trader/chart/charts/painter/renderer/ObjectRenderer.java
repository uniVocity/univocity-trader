package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;
import java.util.function.*;

public abstract class ObjectRenderer<O, T extends Theme> implements Renderer<T> {

	private int i;
	private O[] objects;
	private final Supplier<O> valueSupplier;
	protected final T theme;
	private final IntFunction<O[]> arrayGenerator;

	public ObjectRenderer(T theme, IntFunction<O[]> arrayGenerator, Supplier<O> valueSupplier) {
		this.arrayGenerator = arrayGenerator;
		this.valueSupplier = valueSupplier;
		this.theme = theme;
	}

	@Override
	public final void reset(int length) {
		objects = arrayGenerator.apply(length);
		i = 0;
	}

	@Override
	public final void updateValue() {
		if (i < objects.length) {
			objects[i] = valueSupplier.get();
		}
	}

	@Override
	public final void nextValue() {
		if (i < objects.length) {
			objects[i++] = valueSupplier.get();
		}
	}

	@Override
	public final void paintNext(int i, BasicChart<?> chart, Graphics2D g, AreaPainter areaPainter) {
		if (i < objects.length) {
			paintNext(i, objects[i], chart, g, areaPainter);
		}
	}

	@Override
	public final T getTheme() {
		return theme;
	}

	protected abstract void paintNext(int i, O value, BasicChart<?> chart, Graphics2D g, AreaPainter areaPainter);
}
