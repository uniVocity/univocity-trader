package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public abstract class CandleRenderer<T extends Theme> extends AbstractRenderer<T> {

	private Candle candle;

	public CandleRenderer(String description, T theme) {
		super(description, theme);
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

	public final void paintNext(int i, BasicChart<?> chart, Graphics2D g, AreaPainter painter) {
		Candle candle = chart.candleHistory.get(i);
		paintNext(i, candle, chart, g, painter);
	}

	protected abstract void paintNext(int i, Candle candle, BasicChart<?> chart, Graphics2D g, AreaPainter painter);
}
