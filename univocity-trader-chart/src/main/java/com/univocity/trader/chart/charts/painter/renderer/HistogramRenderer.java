package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class HistogramRenderer extends DoubleRenderer<HistogramTheme<?>> {

	private Point previousZeroLineLocation;

	public HistogramRenderer(String description, HistogramTheme<?> theme, DoubleSupplier valueSupplier) {
		super(description, theme, valueSupplier);
	}

	@Override
	public void paintNext(int i, double value, BasicChart<?> chart, Graphics2D g, AreaPainter painter) {
		drawBar(i, value, chart, g, painter, theme.getFillColor(value), theme.getLineColor(value));
	}

	@Override
	protected void updateSelection(int i, double value, Candle candle, Point candleLocation, BasicChart<?> chart, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		super.updateSelection(i, value, candle, candleLocation, chart, g, painter, headerLine);
		drawBar(i, value, chart, g, painter, theme.getSelectionFillColor(value), theme.getSelectionLineColor(value));
	}

	private void drawBar(int i, double value, BasicChart<?> chart, Graphics2D g, AreaPainter painter, Color fillColor, Color lineColor) {
		g.setStroke(theme.getNormalStroke());
		g.setColor(theme.getLineColor(value));

		Point location = Painter.createCoordinate(chart, painter, i, value);
		Point zeroLineLocation = Painter.createCoordinate(chart, painter, i, 0);

		int areaHeight = painter.bounds().y + painter.bounds().height;
		int zeroLineHeight = areaHeight - zeroLineLocation.y;
		int barHeight = location.y - zeroLineLocation.y;
		barHeight *= 2.0;
		if (barHeight == 0 && value != 0) {
			barHeight = value > 0 ? 1 : -1;
		}

		if (barHeight < 0) {
			barHeight = -barHeight;
			zeroLineHeight += barHeight;
		}

		g.setColor(fillColor);
		g.fillRect(location.x - theme.getBarWidth() / 2, areaHeight - zeroLineHeight, theme.getBarWidth(), barHeight);

		g.setColor(lineColor);
		g.drawRect(location.x - theme.getBarWidth() / 2, areaHeight - zeroLineHeight, theme.getBarWidth(), barHeight);

		g.setColor(theme.getZeroLineColor());

		if (i == 0) {
			previousZeroLineLocation = zeroLineLocation;
		}
		g.drawLine(previousZeroLineLocation.x, previousZeroLineLocation.y, zeroLineLocation.x, zeroLineLocation.y);
		previousZeroLineLocation = zeroLineLocation;
	}
}
