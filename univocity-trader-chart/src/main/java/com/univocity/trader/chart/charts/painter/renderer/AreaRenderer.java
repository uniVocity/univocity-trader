package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class AreaRenderer extends CompositeRenderer<AreaTheme> {

	private final LineRenderer line1;
	private final LineRenderer line2;

	private Point prev1;
	private Point prev2;

	public AreaRenderer(String description, AreaTheme areaTheme, Renderer[] renderers) {
		super(description, areaTheme, renderers);
		this.line1 = (LineRenderer) renderers[0];
		this.line2 = (LineRenderer) renderers[1];
	}

	@Override
	public void paintNext(int i, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter) {
		line1.paintNext(i, chart, overlay, null, painter);
		line2.paintNext(i, chart, overlay, null, painter);

		Point p1 = line1.previousLocation;
		Point p2 = line2.previousLocation;

		if (prev1 != null) {
			g.setColor(p2.y > p1.y ? theme.getNegativeColor() : theme.getPositiveColor());

			int[] xPoints = {prev1.x, p1.x, p2.x, prev2.x};
			int[] yPoints = {prev1.y, p1.y, p2.y, prev2.y};

			g.fillPolygon(xPoints, yPoints, 4);
		}
		prev1 = p1;
		prev2 = p2;
	}

	public void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {

	}
}
