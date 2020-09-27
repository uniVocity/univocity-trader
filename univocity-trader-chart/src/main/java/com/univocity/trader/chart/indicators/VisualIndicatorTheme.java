package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.dynamic.*;

import javax.swing.*;

@UIBound
public class VisualIndicatorTheme extends Theme {

	final VisualIndicator indicator;

	public VisualIndicatorTheme(VisualIndicator indicator) {
		this.indicator = indicator;
	}

	@Override
	public void invokeRepaint() {
		if(indicator.chart != null){
			indicator.chart.invokeRepaint();
		}
	}
}
