package com.univocity.trader.chart;


import java.awt.*;

public class BarChartController extends AbstractBarChartController<BarChart> {

	public BarChartController(BarChart chart) {
		super(chart);
		setBackgroundColor(Color.BLACK);
		setCandleWidth(5);
		setSpaceBetweenCandles(3);
		setStroke(2);
	}

}
