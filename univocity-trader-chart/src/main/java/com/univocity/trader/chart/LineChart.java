package com.univocity.trader.chart;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.annotation.*;

import java.awt.*;

public class LineChart extends InteractiveChart<LineChartController> {

	public LineChart() {
		super();
	}

	@Override
	protected LineChartController newController() {
		return new LineChartController(this);
	}

	@Override
	protected double getHighestPlottedValue(Candle candle) {
		return candle.close;
	}

	@Override
	protected double getLowestPlottedValue(Candle candle) {
		return candle.close;
	}

	@Override
	protected double getCentralValue(Candle candle) {
		return candle.close;
	}

	private Color getLineColor(){
		return getController().getLineColor();
	}

	private Stroke getLineStroke(){
		return getController().getNormalStroke();
	}

	@Override
	protected void draw(Graphics2D g) {
		g.setStroke(getLineStroke());
		g.setColor(getLineColor());

		Point startPoint = null;
		for (int i = 0; i < tradeHistory.size(); i++) {
			Point location = createCandleCoordinate(i);
			if (startPoint == null) {
				startPoint = location;
				continue;
			}

			g.drawLine(startPoint.x, startPoint.y, location.x, location.y);
			startPoint = location;
		}

		super.draw(g);
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

	private void drawCircle(Color color, Point location, Graphics2D g){
		g.setColor(color);
		int markerWidth = getMarkerWidth();
		g.fillOval(location.x - markerWidth / 2, location.y - markerWidth / 2, markerWidth, markerWidth);
	}

}
