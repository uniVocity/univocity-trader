package com.univocity.trader.chart.charts.theme;


import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;

public class HistogramTheme<C extends Repaintable> extends AbstractFilledBarTheme<C> {

	@Label("Zero line color")
	@ColorBound()
	private Color zeroLineColor = new Color(128, 128, 128);;

	public HistogramTheme(C chart) {
		super(chart);
		this.setBarWidth(3);
		this.setDisplayingLogarithmicScale(false);
	}

	public Color getZeroLineColor() {
		return zeroLineColor;
	}

	public void setZeroLineColor(Color zeroLineColor) {
		this.zeroLineColor = zeroLineColor;
	}
}
