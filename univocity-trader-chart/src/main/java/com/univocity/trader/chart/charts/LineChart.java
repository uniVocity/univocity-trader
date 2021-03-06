package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class LineChart extends BasicChart<LineTheme<LineChart>> {

	public LineChart(CandleHistoryView candleHistory) {
		super(candleHistory);
	}

	@Override
	protected LineTheme<LineChart> newTheme() {
		return new LineTheme<>(this);
	}

	@Override
	public double getHighestPlottedValue(Candle candle) {
		return candle.close;
	}

	@Override
	public double getLowestPlottedValue(Candle candle) {
		return candle.close;
	}

	@Override
	protected void prepareToDraw(Graphics2D g) {
		g.setStroke(getLineStroke());
		g.setColor(getLineColor());
	}

	@Override
	protected void doDraw(Graphics2D g, int i, Candle candle, Point current, Point previous) {
		if(previous == null){
			return;
		}
		g.drawLine(previous.x, previous.y, current.x, current.y);
	}

	@Override
	protected void drawSelected(Candle selected, Point location, Graphics2D g) {
		drawCircle(getMarkerColor(), location, g);
	}

	@Override
	protected void drawHovered(Candle hovered, Point location, Graphics2D g) {
		drawCircle(getMarkerColor(), location, g);
	}

	private int getMarkerWidth() {
		return theme().getMarkerWidth();
	}

	private Color getMarkerColor() {
		return theme().getMarkerColor();
	}

	protected final Color getLineColor() {
		return theme().getLineColor();
	}

	private void drawCircle(Color color, Point location, Graphics2D g) {
		g.setColor(color);
		int markerWidth = getMarkerWidth();
		g.fillOval(location.x - markerWidth / 2, location.y - markerWidth / 2, markerWidth, markerWidth);
	}

}
