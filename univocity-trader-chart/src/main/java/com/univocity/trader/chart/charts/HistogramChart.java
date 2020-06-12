package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class HistogramChart extends FilledBarChart<HistogramTheme<HistogramChart>> {

	private final Function<Candle, Double> valueReader;

	public HistogramChart(CandleHistoryView candleHistory) {
		this(candleHistory, (c) -> c.volume);
	}

	public HistogramChart(CandleHistoryView candleHistory, Function<Candle, Double> valueReader) {
		super(candleHistory);
		this.valueReader = valueReader;
	}

	@Override
	protected void drawBar(Candle trade, Point location, Graphics2D g, Color lineColor, Color fillColor) {
		int height = getAvailableHeight();
		int h = height - getYCoordinate(getCentralValue(trade));
		g.setColor(fillColor);
		g.fillRect(location.x - getBarWidth() / 2, height - h, getBarWidth(), h);

		g.setColor(lineColor);
		g.drawRect(location.x - getBarWidth() / 2, height - h, getBarWidth(), h);
	}

	@Override
	public HistogramTheme<HistogramChart> newTheme() {
		return new HistogramTheme<>(this);
	}

	public double getHighestPlottedValue(Candle candle) {
		return valueReader.apply(candle);
	}

	public double getLowestPlottedValue(Candle candle) {
		return valueReader.apply(candle);
	}

	public double getCentralValue(Candle candle) {
		return valueReader.apply(candle);
	}
}
