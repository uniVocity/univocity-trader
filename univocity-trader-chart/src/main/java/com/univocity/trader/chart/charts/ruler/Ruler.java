package com.univocity.trader.chart.charts.ruler;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;
import java.text.*;

public abstract class Ruler<C extends RulerController<?>> implements Painter<C> {

	protected final BasicChart<?> chart;
	private C controller;

	public Ruler(BasicChart<?> chart) {
		this.chart = chart;
		chart.register(this);
	}

	public final void paintOn(Graphics2D g, int width) {
		drawBackground(g, width);

		Candle candle = chart.getCurrentCandle();
		Point location = chart.getCurrentCandleLocation();

		if (candle != null && location != null) {
			drawSelection(g, width, candle, location);
		}
	}

	protected abstract void drawBackground(Graphics2D g, int width);

	protected abstract void drawSelection(Graphics2D g, int width, Candle selectedCandle, Point location);

	protected String readFieldFormatted(Candle candle) {
		return getValueFormat().format(chart.getCentralValue(candle));
	}

	protected abstract Format getValueFormat();

	public final C getController() {
		if (controller == null) {
			controller = newController();
		}
		return controller;
	}

	protected abstract C newController();
}
