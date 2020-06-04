package com.univocity.trader.chart.charts.theme;

import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.*;

public class CandleTheme extends AbstractFilledBarTheme<CandleChart> {


	@Label("Candle width")
	@SpinnerBound(maximum = 20, minimum = 3, increment = 2)
	private int barWidth = 1;

	public CandleTheme(CandleChart chart) {
		super(chart);
		setBarWidth(6);
	}


	@Override
	public int getBarWidth() {
		return barWidth;
	}

	@Override
	public void setBarWidth(int bodyWidth) {
		if (bodyWidth < 3) {
			bodyWidth = 3;
		}
		if (bodyWidth % 2 == 1) {
			barWidth = bodyWidth + 1;
		} else {
			barWidth = bodyWidth;
		}
	}
}
