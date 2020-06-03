package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public interface Painter<C extends Controller> extends Repaintable {

	Insets NO_INSETS = new Insets(0, 0, 0, 0);

	enum Z {
		FRONT, BACK
	}

	Z getZ();

	void paintOn(BasicChart<?> chart, Graphics2D g, int width);

	C getController();

	default Insets insets(){
		return NO_INSETS;
	}

	default void install(BasicChart<?> chart){

	}

	default void uninstall(BasicChart<?> chart){

	}
}
