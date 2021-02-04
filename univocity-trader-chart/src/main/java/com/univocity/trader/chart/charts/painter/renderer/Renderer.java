package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public interface Renderer<T extends Theme> {

	String description();

	T getTheme();

	void reset(int length);

	void updateValue(Candle candle);

	void nextValue(Candle candle);

	double getValueAt(int i);

	Color getColorAt(int i);

	void paintNext(int i, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter);

	void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter, StringBuilder headerLine);

	default boolean displayValue() {
		return true;
	}

	default double getMaximumValue(int from, int to) {
		return Integer.MIN_VALUE;
	}

	default double getMinimumValue(boolean logScale, int from, int to) {
		return Integer.MAX_VALUE;
	}

}
