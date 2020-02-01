package com.univocity.trader.chart.charts.ruler;

import com.univocity.trader.chart.annotation.*;

public class RulerUpdateProcessor implements UpdateProcessor {

	protected RulerController<?> controller;

	public RulerUpdateProcessor(RulerController<?> controller) {
		this.controller = controller;
	}

	@Override
	public void execute() {
		if (controller != null) {
			controller.getRuler().chart.invokeRepaint();
			;
		}
	}
}
