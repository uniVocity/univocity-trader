package com.univocity.trader.chart.charts.theme.indicator;

import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class LowerBand<T extends Repaintable> extends LineTheme<T> {
	public LowerBand(T chart) {
		super(chart);
		setLineColor(new Color(255, 128, 128, 160));
	}

}
