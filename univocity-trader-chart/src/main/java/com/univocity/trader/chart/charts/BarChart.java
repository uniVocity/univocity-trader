package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;

public class BarChart extends BasicChart<BarChartController> {

	public BarChart(CandleHistoryView candleHistory) {
		super(candleHistory);
	}

	@Override
	protected void draw(Graphics2D g, int width) {
		for (int i = 0; i < candleHistory.size(); i++) {
			Point location = createCandleCoordinate(i);
			Candle candle = candleHistory.get(i);
			drawBar(getLineColor(candle), candle, location, g);
		}

		super.draw(g, width);
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
		return getController().getLineColor(candle);
	}

	private Color getLineSelectionColor(Candle candle) {
		return getController().getSelectionLineColor(candle);
	}

	@Override
	public BarChartController newController() {
		return new BarChartController(this);
	}

	@Override
	protected void drawSelected(Candle selected, Point location, Graphics2D g) {
		drawSelectedBar(selected, location, g);
	}

	@Override
	protected void drawHovered(Candle hovered, Point location, Graphics2D g) {
		drawSelectedBar(hovered, location, g);
	}

	private void drawSelectedBar(Candle selected, Point location, Graphics2D g){
		drawBar(getLineSelectionColor(selected), selected, location, g);
	}


}
