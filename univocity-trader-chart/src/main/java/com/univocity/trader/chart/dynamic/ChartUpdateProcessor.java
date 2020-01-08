package com.univocity.trader.chart.dynamic;

import com.univocity.trader.chart.*;
import com.univocity.trader.chart.annotation.*;

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
