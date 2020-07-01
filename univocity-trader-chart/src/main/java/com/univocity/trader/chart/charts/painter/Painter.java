package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public interface Painter<T extends Theme> extends Repaintable {

	Insets NO_INSETS = new Insets(0, 0, 0, 0);

	enum Overlay {
		FRONT, BACK, NONE
	}

	Overlay overlay();

	void paintOn(BasicChart<?> chart, Graphics2D g, int width, Overlay overlay);

	T theme();

	default String header() {
		return null;
	}

	default Insets insets() {
		return NO_INSETS;
	}

	default Rectangle bounds() {
		return null;
	}

	default void install(BasicChart<?> chart) {

	}

	default void uninstall(BasicChart<?> chart) {

	}

	default void position(int position){

	}

	default int position() {
		return -1;
	}

	default double maximumValue(int from, int to) {
		return Integer.MIN_VALUE;
	}

	default double minimumValue(boolean logScale, int from, int to) {
		return Integer.MAX_VALUE;
	}

	static Point createCoordinate(BasicChart<?> chart, AreaPainter painter, Overlay overlay, int candleIndex, double value) {
		Point p = new Point();
		p.x = chart.getXCoordinate(candleIndex);
		if (painter == null || painter.bounds() == null) {
			p.y = chart.getYCoordinate(value);
		} else {
			Rectangle bounds = painter.bounds();
			p.y = bounds.y + painter.getYCoordinate(overlay, value, bounds.height);
		}

		return p;
	}
}
