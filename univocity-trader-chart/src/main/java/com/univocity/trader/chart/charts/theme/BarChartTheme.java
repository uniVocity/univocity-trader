package com.univocity.trader.chart.charts.theme;


import com.univocity.trader.chart.charts.*;

public class BarChartTheme extends AbstractBarTheme<BarChart> {

	public BarChartTheme(BarChart chart) {
		super(chart);
		setBarWidth(5);
		setSpaceBetweenBars(3);
		setStroke(2);
	}

}
