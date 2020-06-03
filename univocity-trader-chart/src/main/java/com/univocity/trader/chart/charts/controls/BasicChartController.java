package com.univocity.trader.chart.charts.controls;


import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.*;

@UIBound
public class BasicChartController<T extends BasicChart<?>> extends PlotController<T> {

	public BasicChartController(T chart) {
		super(chart);
	}
}

