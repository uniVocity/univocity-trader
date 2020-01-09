package com.univocity.trader.chart.charts.controls;


import com.univocity.trader.chart.charts.*;

import java.awt.*;

public class BarChartController extends AbstractBarChartController<BarChart> {

	public BarChartController(BarChart chart) {
		super(chart);
		setCandleWidth(5);
		setSpaceBetweenCandles(3);
		setStroke(2);
	}

}
