package com.univocity.trader.chart.charts.controls;

import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.*;

public class CandleChartController extends AbstractFilledBarChartController<CandleChart> {


	@Label("Candle width")
	@SpinnerBound(maximum = 20, minimum = 3, increment = 2)
	private int candleWidth = 1;

	public CandleChartController(CandleChart chart) {
		super(chart);
		setCandleWidth(6);
	}


	@Override
	public int getCandleWidth() {
		return candleWidth;
	}

	@Override
	public void setCandleWidth(int bodyWidth) {
		if (bodyWidth < 3) {
			bodyWidth = 3;
		}
		if (bodyWidth % 2 == 1) {
			candleWidth = bodyWidth + 1;
		} else {
			candleWidth = bodyWidth;
		}
	}
}
