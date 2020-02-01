package com.univocity.trader.chart.charts.controls;

import com.univocity.trader.chart.charts.*;

public class BarChartController extends AbstractBarChartController<BarChart> {

	public BarChartController(BarChart chart) {
		super(chart);
		setBarWidth(5);
		setSpaceBetweenBars(3);
		setStroke(2);
	}

}
