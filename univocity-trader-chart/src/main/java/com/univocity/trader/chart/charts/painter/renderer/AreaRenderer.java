package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class AreaRenderer extends CompositeRenderer<AreaTheme> {

	private final LineRenderer line1;
	private final LineRenderer line2;

	public AreaRenderer(String description, AreaTheme areaTheme, Renderer[] renderers) {
		super(description, areaTheme, renderers);
		this.line1 = (LineRenderer) renderers[0];
		this.line2 = (LineRenderer) renderers[1];
	}

	@Override
	public void paintNext(int i, BasicChart<?> chart, Graphics2D g, AreaPainter painter) {
		line1.paintNext(i, chart, null, painter);
		line2.paintNext(i, chart, null, painter);

		int yTop = line1.previousLocation.y;
		int yBottom = line2.previousLocation.y;

		int x = line1.previousLocation.x;

		int y;
		int height;
		Color color;
		if (yBottom > yTop) {
			height = yBottom - yTop;
			color = theme.getNegativeColor();
			y = yTop;
		} else {
			height = yTop - yBottom;
			color = theme.getPositiveColor();
			y = yBottom;
		}

		g.setColor(color);
		g.fillRect(x, y, chart.getBarWidth() + chart.getSpaceBetweenCandles(), height);

//		g.setColor(Color.ORANGE);
//		int[] xPoints = {x, x + chart.getBarWidth() + chart.getSpaceBetweenCandles()};
//		int[] yPoints = {y, y + height};

//		g.fillPolygon(xPoints, yPoints, 2);

	}

	@Override
	public void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		headerLine.append(line1.description()).append(":").append(line1.getValueAt(i));
		headerLine.append(", ");
		headerLine.append(line2.description()).append(":").append(line2.getValueAt(i));
	}
}
