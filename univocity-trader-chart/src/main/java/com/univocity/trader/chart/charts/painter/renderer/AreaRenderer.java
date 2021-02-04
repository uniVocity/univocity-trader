package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class AreaRenderer extends CompositeRenderer<AreaTheme> {

	private LineRenderer line1;
	private LineRenderer line2;
	private SignalRenderer signalRenderer;

	private Point prev1;
	private Point prev2;

	public AreaRenderer(String description, AreaTheme areaTheme, Renderer[] renderers) {
		super(description, areaTheme, renderers);
		for (int i = 0; i < renderers.length; i++) {
			if (renderers[i] instanceof LineRenderer) {
				if (line1 == null) {
					line1 = (LineRenderer) renderers[i];
				} else {
					line2 = (LineRenderer) renderers[i];
				}
			} else if (renderers[i] instanceof SignalRenderer) {
				signalRenderer = (SignalRenderer) renderers[i];
			}
		}
	}

	@Override
	public Color getColorAt(int i) {
		return null;
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

	@Override
	public void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		signalRenderer.updateSelection(i, candle, candleLocation, chart, overlay, g, painter, headerLine);
	}
}
