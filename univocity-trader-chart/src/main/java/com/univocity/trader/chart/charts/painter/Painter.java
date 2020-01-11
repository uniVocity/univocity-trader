package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public interface Painter<C extends Controller> {
	enum Z {
		FRONT, BACK
	}

	Z getZ();

	void paintOn(Graphics2D g, int width);

	C getController();
}
