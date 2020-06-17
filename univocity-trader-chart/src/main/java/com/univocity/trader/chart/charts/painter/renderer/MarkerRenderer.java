package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class MarkerRenderer extends DoubleRenderer<MarkerTheme<?>> {

	public MarkerRenderer(String description, MarkerTheme<?> markerTheme, DoubleSupplier valueSupplier) {
		super(description, markerTheme, valueSupplier);
	}

	@Override
	public void paintNext(int i, double value, BasicChart<?> chart, Graphics2D g, AreaPainter painter) {
		paintMarker(i, value, chart, g, painter, theme.getMarkerColor());
	}

	@Override
	protected void updateSelection(int i, double value, Candle candle, Point candleLocation, BasicChart<?> chart, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		super.updateSelection(i, value, candle, candleLocation, chart, g, painter, headerLine);
		paintMarker(i, value, chart, g, painter, theme.getMarkerColor());
	}

	private void paintMarker(int i, double value, BasicChart<?> chart, Graphics2D g, AreaPainter painter, Color markerColor){
		g.setColor(markerColor);
		int markerWidth = theme.getMarkerWidth();
		Point location = Painter.createCoordinate(chart, painter, i, value);
		g.fillOval(location.x - markerWidth / 2, location.y - markerWidth / 2, markerWidth, markerWidth);
	}
}
