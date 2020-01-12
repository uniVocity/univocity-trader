package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;

public class LineChart extends InteractiveChart<LineChartController> {

	public LineChart(CandleHistoryView candleHistory) {
		super(candleHistory);
	}

	@Override
	protected LineChartController newController() {
		return new LineChartController(this);
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
		return getController().getMarkerWidth();
	}

	private Color getMarkerColor(){
		return getController().getMarkerColor();
	}

	protected final Color getLineColor() {
		return getController().getLineColor();
	}

	private void drawCircle(Color color, Point location, Graphics2D g){
		g.setColor(color);
		int markerWidth = getMarkerWidth();
		g.fillOval(location.x - markerWidth / 2, location.y - markerWidth / 2, markerWidth, markerWidth);
	}

}
