package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class HistogramRenderer extends DoubleRenderer<HistogramTheme<?>> {

	private Point previousZeroLineLocation;

	public HistogramRenderer(HistogramTheme<?> theme, DoubleSupplier valueSupplier) {
		super(theme, valueSupplier);
	}

	@Override
	public void paintNext(int i, double value, BasicChart<?> chart, Graphics2D g, AreaPainter painter) {
		g.setStroke(theme.getNormalStroke());
		g.setColor(theme.getLineColor(value));

		Point location = Painter.createCoordinate(chart, painter, i, value);
		Point zeroLineLocation = Painter.createCoordinate(chart, painter, i, 0);

		int areaHeight = painter.bounds().y + painter.bounds().height;
		int zeroLineHeight = areaHeight - zeroLineLocation.y;
		int barHeight = location.y - zeroLineLocation.y;
		barHeight *= 2.0;
		if(barHeight == 0 && value != 0){
			barHeight = value > 0 ? 1 : -1;
		}

		if(barHeight < 0){
			barHeight = -barHeight;
			zeroLineHeight += barHeight;
		}

		g.setColor(theme.getFillColor(value));
		g.fillRect(location.x - theme.getBarWidth() / 2, areaHeight - zeroLineHeight, theme.getBarWidth(), barHeight);

		g.setColor(theme.getLineColor(value));
		g.drawRect(location.x - theme.getBarWidth() / 2, areaHeight - zeroLineHeight, theme.getBarWidth(), barHeight);

		g.setColor(theme.getZeroLineColor());

		if (i == 0) {
			previousZeroLineLocation = zeroLineLocation;
		}
		g.drawLine(previousZeroLineLocation.x, previousZeroLineLocation.y, zeroLineLocation.x, zeroLineLocation.y);
		previousZeroLineLocation = zeroLineLocation;
	}
}
