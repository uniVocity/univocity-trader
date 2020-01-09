package com.univocity.trader.chart.dynamic;

import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.controls.*;

public class ChartUpdateProcessor implements UpdateProcessor {

	private BasicChartController controller;

	public ChartUpdateProcessor(BasicChartController controller) {
		this.controller = controller;
	}

	@Override
	public void execute() {
		if (controller != null && controller.getChart() != null) {
			controller.getChart().repaint();
		}
	}
}
