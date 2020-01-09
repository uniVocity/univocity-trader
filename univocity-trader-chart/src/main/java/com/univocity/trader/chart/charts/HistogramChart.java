package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;
import java.util.function.*;

public class HistogramChart extends FilledBarChart<HistogramChartController> {

	private final Function<Candle, Double> valueReader;

	public HistogramChart() {
		this((c) -> c.volume);
	}

	public HistogramChart(Function<Candle, Double> valueReader) {
		this.valueReader = valueReader;
	}

	@Override
	protected void drawBar(Candle trade, Point location, Graphics2D g, Color lineColor, Color fillColor) {
		int h = height - getYCoordinate(getCentralValue(trade));
		g.setColor(fillColor);
		g.fillRect(location.x - getCandleWidth() / 2, height - h, getCandleWidth(), h);

		g.setColor(lineColor);
		g.drawRect(location.x - getCandleWidth() / 2, height - h, getCandleWidth(), h);
	}

	@Override
	public HistogramChartController newController() {
		return new HistogramChartController(this);
	}

	protected double getHighestPlottedValue(Candle candle) {
		return valueReader.apply(candle);
	}

	protected double getLowestPlottedValue(Candle candle) {
		return valueReader.apply(candle);
	}

	protected double getCentralValue(Candle candle) {
		return valueReader.apply(candle);
	}
}
