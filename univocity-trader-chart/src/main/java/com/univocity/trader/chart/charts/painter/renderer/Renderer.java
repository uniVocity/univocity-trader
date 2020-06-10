package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public interface Renderer<T extends Theme> {

	T getTheme();

	void reset(int length);

	void updateValue();

	void nextValue();

	void paintNext(int i, BasicChart<?> chart, Graphics2D g, int width);

	default double getMaximumValue(int from, int to) {
		return Integer.MIN_VALUE;
	}

	default double getMinimumValue(int from, int to) {
		return Integer.MAX_VALUE;
	}
}
