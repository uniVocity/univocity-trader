package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class LineRenderer extends DoubleRenderer<LineTheme<?>> {

	private Point previousLocation;

	public LineRenderer(LineTheme<?> lineTheme, DoubleSupplier valueSupplier) {
		super(lineTheme, valueSupplier);
	}

	@Override
	public void paintNext(int i, double value, BasicChart<?> chart, Graphics2D g, int y, int height, int width) {
		g.setStroke(theme.getNormalStroke());
		g.setColor(theme.getLineColor());

		Point location = Painter.createCoordinate(chart, i, value);
		if (i == 0) {
			previousLocation = location;
		}
		g.drawLine(previousLocation.x, previousLocation.y, location.x, location.y);
		previousLocation = location;
	}
}
