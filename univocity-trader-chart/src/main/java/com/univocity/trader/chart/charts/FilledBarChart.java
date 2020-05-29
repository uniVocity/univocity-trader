package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;

abstract class FilledBarChart<C extends AbstractFilledBarChartController<?>> extends BasicChart<C> {

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
		return getController().getFillColor(candle);
	}

	private Color getLineColor(Candle candle) {
		return getController().getLineColor(candle);
	}

	private Color getSelectionLineColor(Candle selected) {
		return getController().getSelectionLineColor(selected);
	}

	private Color getSelectionFillColor(Candle selected) {
		return getController().getSelectionFillColor(selected);
	}
}
