package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class LineChart extends BasicChart<LinePlotTheme<LineChart>> {

	public LineChart(CandleHistoryView candleHistory) {
		super(candleHistory);
	}

	@Override
	protected LinePlotTheme<LineChart> newTheme() {
		return new LinePlotTheme<>(this);
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
	protected void draw(Graphics2D g, int width) {
		g.setStroke(getLineStroke());
		g.setColor(getLineColor());

		Point startPoint = null;
		for (int i = 0; i < candleHistory.size(); i++) {
			Point location = createCandleCoordinate(i);
			if (startPoint == null) {
				startPoint = location;
				continue;
			}

			g.drawLine(startPoint.x, startPoint.y, location.x, location.y);
			startPoint = location;
		}

		super.draw(g, width);
	}

	@Override
	protected void drawSelected(Candle selected, Point location, Graphics2D g) {
		drawCircle(getMarkerColor(), location, g);
	}

	@Override
	protected void drawHovered(Candle hovered, Point location, Graphics2D g) {
		drawCircle(getMarkerColor(), location, g);
	}

	private int getMarkerWidth(){
		return getTheme().getMarkerWidth();
	}

	private Color getMarkerColor(){
		return getTheme().getMarkerColor();
	}

	protected final Color getLineColor() {
		return getTheme().getLineColor();
	}

	private void drawCircle(Color color, Point location, Graphics2D g){
		g.setColor(color);
		int markerWidth = getMarkerWidth();
		g.fillOval(location.x - markerWidth / 2, location.y - markerWidth / 2, markerWidth, markerWidth);
	}

}
