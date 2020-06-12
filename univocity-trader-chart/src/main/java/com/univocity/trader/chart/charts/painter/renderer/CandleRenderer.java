package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public abstract class CandleRenderer<T extends Theme> implements Renderer<T> {

	private Candle candle;

	protected final T theme;

	public CandleRenderer(T theme) {
		this.theme = theme;
	}

	@Override
	public final void reset(int length) {

	}

	@Override
	public final void updateValue() {

	}

	@Override
	public final void nextValue() {

	}

	@Override
	public final T getTheme(){
		return theme;
	}

	public final void paintNext(int i, BasicChart<?> chart, Graphics2D g, int y, int height, int width) {
		Candle candle = chart.candleHistory.get(i);
		paintNext(i, candle, chart, g, width);
	}

	protected abstract void paintNext(int i, Candle candle, BasicChart<?> chart, Graphics2D g, int width);
}
