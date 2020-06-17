package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class LineRenderer extends DoubleRenderer<LineTheme<?>> {

	Point previousLocation;

	public LineRenderer(String description, LineTheme<?> lineTheme, DoubleSupplier valueSupplier) {
		super(description, lineTheme, valueSupplier);
	}

	@Override
	public void paintNext(int i, double value, BasicChart<?> chart, Graphics2D g, AreaPainter painter) {
		if(g != null) {
			g.setStroke(theme.getNormalStroke());
			g.setColor(theme.getLineColor());

			Point location = Painter.createCoordinate(chart, painter, i, value);
			if (i == 0) {
				previousLocation = location;
			}
			g.drawLine(previousLocation.x, previousLocation.y, location.x, location.y);
			previousLocation = location;
		} else {
			previousLocation = Painter.createCoordinate(chart, painter, i, value);
		}
	}

	@Override
	protected void updateSelection(int i, double value, Candle candle, Point candleLocation, BasicChart<?> chart, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		super.updateSelection(i, value, candle, candleLocation, chart, g, painter, headerLine);

		g.setColor(theme.getMarkerColor());
		int markerWidth = theme.getMarkerWidth();

		Point location = Painter.createCoordinate(chart, painter, i, value);
		g.fillOval(candleLocation.x - markerWidth / 2, location.y - markerWidth / 2, markerWidth, markerWidth);
	}
}
