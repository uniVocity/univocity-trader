package com.univocity.trader.chart.charts.controls;


import com.univocity.trader.chart.charts.*;

public class HistogramChartController extends AbstractFilledBarChartController<HistogramChart> {

	public HistogramChartController(HistogramChart chart) {
		super(chart);
		this.setBarWidth(3);
		this.setDisplayingLogarithmicScale(false);
	}
}
