package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;

public class CandleChart extends InteractiveChart<CandleChartController> {

	public CandleChart() {
	}

	@Override
	protected void draw(Graphics2D g) {
		for (int i = 0; i < tradeHistory.size(); i++) {
			Point location = createCandleCoordinate(i);
			Candle candle = tradeHistory.get(i);
			drawCandle(candle, location, g, getLineColor(candle), getFillColor(candle));
		}

		super.draw(g);
	}

	private void drawCandle(Candle trade, Point location, Graphics2D g, Color lineColor, Color fillColor) {
		g.setStroke(getLineStroke());
		int high = getYCoordinate(trade.high);
		int low = getYCoordinate(trade.low);
		int close = getYCoordinate(trade.close);
		int open = getYCoordinate(trade.open);

		int x = location.x;

		int bodyWidth = getCandleWidth();

		if (trade.isClosePositive()) {
			if (getController().getPositiveClosingFilled()) {
				g.setColor(fillColor);
				g.fillRect(x - (bodyWidth / 2), close, bodyWidth + 1, open - close + 1);
			}
			g.setColor(lineColor);
			g.drawRect(x - (bodyWidth / 2), close, bodyWidth, open - close);

			g.drawLine(x, close, x, high);
			g.drawLine(x, open, x, low);
		} else {
			if (getController().getNegativeClosingFilled()) {
				g.setColor(fillColor);
				g.fillRect(x - (bodyWidth / 2), open, bodyWidth + 1, close - open + 1);
			}

			g.setColor(lineColor);
			g.drawRect(x - (bodyWidth / 2), open, bodyWidth, close - open);

			g.drawLine(x, close, x, low);
			g.drawLine(x, open, x, high);
		}
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
		drawCandle(selected, location, g, getSelectionLineColor(selected), getSelectionFillColor(selected));
	}

	@Override
	public CandleChartController newController() {
		return new CandleChartController(this);
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
