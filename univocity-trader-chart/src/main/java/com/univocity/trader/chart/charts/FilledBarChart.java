package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

abstract class FilledBarChart<C extends AbstractFilledBarTheme<?>> extends BasicChart<C> {

	public FilledBarChart(CandleHistoryView candleHistory) {
		super(candleHistory);
	}

	@Override
	protected final void prepareToDraw(Graphics2D g) {
		g.setStroke(getLineStroke());
	}

	@Override
	protected void doDraw(Graphics2D g, int i, Candle candle, Point current, Point previous) {
		drawBar(candle, current, g, getLineColor(candle), getFillColor(candle));
	}

	protected abstract void drawBar(Candle trade, Point location, Graphics2D g, Color lineColor, Color fillColor);

	@Override
	protected void drawSelected(Candle selected, Point location, Graphics2D g) {
		drawSelectedBar(selected, location, g);
	}

	@Override
	protected void drawHovered(Candle hovered, Point location, Graphics2D g) {
		drawSelectedBar(hovered, location, g);
	}

	private void drawSelectedBar(Candle selected, Point location, Graphics2D g) {
		drawBar(selected, location, g, getSelectionLineColor(selected), getSelectionFillColor(selected));
	}

	private Color getFillColor(Candle candle) {
		return getFillColor(candle.getChange());
	}

	private Color getLineColor(Candle candle) {
		return getLineColor(candle.getChange());
	}

	private Color getSelectionLineColor(Candle selected) {
		return getSelectionLineColor(selected.getChange());
	}

	private Color getSelectionFillColor(Candle selected) {
		return getSelectionFillColor(selected.getChange());
	}

	private Color getFillColor(double value) {
		return theme().getFillColor(value);
	}

	private Color getLineColor(double value) {
		return theme().getLineColor(value);
	}

	private Color getSelectionLineColor(double value) {
		return theme().getSelectionLineColor(value);
	}

	private Color getSelectionFillColor(double value) {
		return theme().getSelectionFillColor(value);
	}
}
