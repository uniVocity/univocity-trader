package com.univocity.trader.chart.charts.theme.indicator;

import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class MiddleBand<T extends Repaintable> extends LineTheme<T> {
	public MiddleBand(T chart) {
		super(chart);
		setLineColor(new Color(0, 64, 0, 160));
		setStroke(2);
	}

}
