package com.univocity.trader.chart.charts.theme.indicator;

import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class BlueLine<T extends Repaintable> extends LineTheme<T> {
	public BlueLine(T chart) {
		super(chart);
		setLineColor(new Color(0, 0, 128, 160));
	}

}
