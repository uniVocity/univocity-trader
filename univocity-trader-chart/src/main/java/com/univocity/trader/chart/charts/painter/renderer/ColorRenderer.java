package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;
import java.util.function.*;

public class ColorRenderer extends ObjectRenderer<Color, Theme> {

	public ColorRenderer(Supplier<Color> valueSupplier) {
		super("", null, Color[]::new, (c) -> valueSupplier.get());
	}

	@Override
	protected void paintNext(int i, Color value, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter areaPainter) {

	}

	@Override
	public double getValueAt(int i) {
		return 0;
	}

	@Override
	public Color getColorAt(int i) {
		return this.objectAt(i);
	}

	@Override
	public void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {

	}
}
