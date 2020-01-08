package com.univocity.trader.chart;


import com.univocity.trader.candles.*;

import java.awt.*;

public class LineChart extends InteractiveChart {

	public LineChart() {
		super();
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

	@Override
	protected void draw(Graphics2D g) {
		g.setStroke(new BasicStroke(1));
		g.setColor(Color.BLACK);

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
		drawCircle(Color.BLUE, location, g);
	}

	@Override
	protected void drawHovered(Candle hovered, Point location, Graphics2D g) {
		drawCircle(Color.DARK_GRAY, location, g);
	}

	private void drawCircle(Color color, Point location, Graphics2D g){
		g.setColor(color);
		int markerWidth = 8;
		g.fillOval(location.x - markerWidth / 2, location.y - markerWidth / 2, markerWidth, markerWidth);
	}

	@Override
	protected int getCandleWidth() {
		return 1;
	}

	@Override
	protected int getSpaceBetweenCandles() {
		return 0;
	}

	public static void main(String... args) {

	}
}
