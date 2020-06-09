package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public interface Painter<T extends Theme> extends Repaintable {

	Insets NO_INSETS = new Insets(0, 0, 0, 0);

	enum Z {
		FRONT, BACK
	}

	Z getZ();

	void paintOn(BasicChart<?> chart, Graphics2D g, int width);

	T getTheme();

	default Insets insets() {
		return NO_INSETS;
	}

	default void install(BasicChart<?> chart) {

	}

	default void uninstall(BasicChart<?> chart) {

	}

	static Point createCoordinate(BasicChart<?> chart, int candleIndex, double value) {
		Point p = new Point();
		p.x = chart.getXCoordinate(candleIndex);
		p.y = chart.getYCoordinate(value);
		return p;
	}
}
