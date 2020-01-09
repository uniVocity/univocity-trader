package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;

public class CandleChart extends FilledBarChart<CandleChartController> {

	public CandleChart(CandleHistory candleHistory) {
		super(candleHistory);
	}

	@Override
	protected void drawBar(Candle trade, Point location, Graphics2D g, Color lineColor, Color fillColor) {
		g.setStroke(getLineStroke());
		int high = getYCoordinate(trade.high);
		int low = getYCoordinate(trade.low);
		int close = getYCoordinate(trade.close);
		int open = getYCoordinate(trade.open);

		int x = location.x;

		int bodyWidth = getBarWidth();

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
	public CandleChartController newController() {
		return new CandleChartController(this);
	}
}
