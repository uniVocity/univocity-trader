package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class ColoredHistogramRenderer extends CompositeRenderer<HistogramTheme<?>> {

	private final HistogramRenderer histogram;
	private final ColorRenderer fillColorRenderer;
	private final ColorRenderer lineColorRenderer;

	public ColoredHistogramRenderer(String description, HistogramTheme<?> theme, DoubleSupplier supplier, Supplier<Boolean> isPositive) {
		this(description, theme, supplier,
				() -> isPositive.get() ? theme.getUpFillColor() : theme.getDownFillColor(),
				() -> isPositive.get() ? theme.getUpColor() : theme.getDownColor()
		);
	}

	public ColoredHistogramRenderer(String description, HistogramTheme<?> theme, DoubleSupplier supplier, Supplier<Color> fillColorSupplier, Supplier<Color> lineColorSupplier) {
		super(description, theme, new Renderer[]{
				new HistogramRenderer(description, theme, supplier),
				new ColorRenderer(fillColorSupplier),
				new ColorRenderer(lineColorSupplier)});

		this.histogram = (HistogramRenderer) renderers[0];
		this.fillColorRenderer = (ColorRenderer) renderers[1];
		this.lineColorRenderer = (ColorRenderer) renderers[2];
	}

	@Override
	public Color getColorAt(int i) {
		if(lineColorRenderer != null){
			return lineColorRenderer.getColorAt(i);
		}
		return null;
	}

	@Override
	public void paintNext(int i, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter) {
		histogram.drawBar(i, histogram.getValueAt(i), chart, overlay, g, painter, fillColorRenderer.objectAt(i), lineColorRenderer.objectAt(i));
	}

	public double getMaximumValue(int from, int to) {
		return histogram.getMaximumValue(from, to);
	}

	public double getMinimumValue(boolean logScale, int from, int to) {
		return histogram.getMinimumValue(logScale, from, to);
	}
}
