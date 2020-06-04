package com.univocity.trader.chart.charts.theme;


import com.univocity.trader.chart.charts.painter.*;

public class HistogramTheme<C extends Repaintable> extends AbstractFilledBarTheme<C> {

	public HistogramTheme(C chart) {
		super(chart);
		this.setBarWidth(3);
		this.setDisplayingLogarithmicScale(false);
	}
}
