package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class BarChart extends BasicChart<BarChartTheme> {

	public BarChart(CandleHistoryView candleHistory) {
		super(candleHistory);
	}

	@Override
	protected final void doDraw(Graphics2D g, int i, Candle candle, Point current, Point previous){
		drawBar(getLineColor(candle), candle, current, g);
	}

	private void drawBar(Color color, Candle candle, Point location, Graphics2D g) {
		g.setColor(color);
		g.setStroke(getLineStroke());

		int high = getYCoordinate(candle.high);
		int low = getYCoordinate(candle.low);
		int close = getYCoordinate(candle.close);
		int open = getYCoordinate(candle.open);

		int x = location.x;

		g.drawLine(x, low, x, high);
		g.drawLine(x - getBarWidth() / 2, open, x, open);
		g.drawLine(x, close, x + getBarWidth() / 2, close);
	}

	private Color getLineColor(Candle candle) {
		return getLineColor(candle.getChange());
	}

	private Color getLineColor(double value) {
		return theme().getLineColor(value);
	}

	private Color getLineSelectionColor(Candle candle) {
		return getLineSelectionColor(candle.getChange());
	}


	private Color getLineSelectionColor(double value) {
		return theme().getSelectionLineColor(value);
	}

	@Override
	public BarChartTheme newTheme() {
		return new BarChartTheme(this);
	}

	@Override
	protected void drawSelected(Candle selected, Point location, Graphics2D g) {
		drawSelectedBar(selected, location, g);
	}

	@Override
	protected void drawHovered(Candle hovered, Point location, Graphics2D g) {
		drawSelectedBar(hovered, location, g);
	}

	private void drawSelectedBar(Candle selected, Point location, Graphics2D g) {
		drawBar(getLineSelectionColor(selected), selected, location, g);
	}


}
