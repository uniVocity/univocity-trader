package com.univocity.trader.chart.charts.ruler;

import com.univocity.trader.chart.annotation.*;

@Border("Time ruler settings")
@UIBoundClass(updateProcessor = RulerUpdateProcessor.class)
public class TimeRulerController extends RulerController<TimeRulerController> {

	public TimeRulerController(TimeRuler ruler) {
		super(ruler);
	}

}
