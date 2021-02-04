package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class HistogramRenderer extends DoubleRenderer<HistogramTheme<?>> {

	private Point previousZeroLineLocation;
	private double previousValue = Integer.MIN_VALUE;

	public HistogramRenderer(String description, HistogramTheme<?> theme, DoubleSupplier valueSupplier) {
		super(description, theme, valueSupplier);
	}

	@Override
	public void paintNext(int i, double value, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter) {
		drawBar(i, value, chart, overlay, g, painter, getFillColor(theme.getFillColor(value), previousValue, value), getLineColor(theme.getLineColor(value), previousValue, value));
	}

	@Override
	protected void updateSelection(int i, double value, Candle candle, Point candleLocation, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		super.updateSelection(i, value, candle, candleLocation, chart, overlay, g, painter, headerLine);
		double previousValue = getValueAt(i - 1);
		drawBar(i, value, chart, overlay, g, painter, getSelectionFillColor(theme.getSelectionFillColor(value), previousValue, value), getSelectionLineColor(theme.getSelectionLineColor(value), previousValue, value));
	}

	void drawBar(int i, double value, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter, Color fillColor, Color lineColor) {
		g.setStroke(theme.getNormalStroke());
		g.setColor(theme.getLineColor(value));

		Point location = Painter.createCoordinate(chart, painter, overlay, i, value);
		Point zeroLineLocation = Painter.createCoordinate(chart, painter, overlay, i, 0);

		int areaHeight = painter.bounds().y + painter.bounds().height;
		int zeroLineHeight = areaHeight - zeroLineLocation.y;
		int barHeight = location.y - zeroLineLocation.y;
//		barHeight *= 2.0;

		boolean onNegativeEdge = false;
		if (barHeight == 0 && value != 0) {
			barHeight = value > 0 ? 1 : -1;
			onNegativeEdge = barHeight < 0;
		}

		if (barHeight < 0) {
			barHeight = -barHeight;
			zeroLineHeight += barHeight;
		}
		if (onNegativeEdge) {
			zeroLineHeight--;
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
		previousValue = value;
	}

	protected Color getLineColor(Color currentLineColor, double previousValue, double currentValue) {
		return currentLineColor;
	}

	protected Color getFillColor(Color currentFillColor, double previousValue, double currentValue) {
		return currentFillColor;
	}

	protected Color getSelectionLineColor(Color currentLineColor, double previousValue, double currentValue) {
		return currentLineColor;
	}

	protected Color getSelectionFillColor(Color currentFillColor, double previousValue, double currentValue) {
		return currentFillColor;
	}

	@Override
	public final Color getColorAt(int i) {
		double value = getValueAt(i);
		return getFillColor(theme.getFillColor(value), getValueAt(i - 1), value);
	}
}

