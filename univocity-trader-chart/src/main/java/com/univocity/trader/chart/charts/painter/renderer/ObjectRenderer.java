package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;
import java.util.function.*;

public abstract class ObjectRenderer<O, T extends Theme> extends AbstractRenderer<T> {

	private int i;
	protected O[] objects;
	private final Function<Candle, O> valueSupplier;
	private final IntFunction<O[]> arrayGenerator;

	public ObjectRenderer(String description, T theme, IntFunction<O[]> arrayGenerator, Function<Candle, O> valueSupplier) {
		super(description, theme);
		this.objects = arrayGenerator.apply(0);
		this.arrayGenerator = arrayGenerator;
		this.valueSupplier = valueSupplier;
	}

	@Override
	public final void reset(int length) {
		objects = arrayGenerator.apply(length);
		i = 0;
	}

	@Override
	public final void updateValue(Candle candle) {
		if (i < objects.length) {
			objects[i] = valueSupplier.apply(candle);
		}
	}

	@Override
	public final void nextValue(Candle candle) {
		if (i < objects.length) {
			objects[i++] = valueSupplier.apply(candle);
		}
	}

	@Override
	public final void paintNext(int i, BasicChart<?> chart, Graphics2D g, AreaPainter areaPainter) {
		if (i < objects.length && objects[i] != null) {
			paintNext(i, objects[i], chart, g, areaPainter);
		}
	}

	protected abstract void paintNext(int i, O value, BasicChart<?> chart, Graphics2D g, AreaPainter areaPainter);
}
