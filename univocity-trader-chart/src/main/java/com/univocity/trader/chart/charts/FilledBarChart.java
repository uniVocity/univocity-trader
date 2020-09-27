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
	protected void draw(Graphics2D g, int width) {
		g.setStroke(getLineStroke());
		for (int i = 0; i < candleHistory.size(); i++) {
			Candle candle = candleHistory.get(i);
			if (candle == null) {
				return;
			}
			Point location = createCandleCoordinate(i);
			drawBar(candle, location, g, getLineColor(candle), getFillColor(candle));
		}

		super.draw(g, width);
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
