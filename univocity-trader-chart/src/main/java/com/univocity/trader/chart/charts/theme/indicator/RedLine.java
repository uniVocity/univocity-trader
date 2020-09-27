package com.univocity.trader.chart.charts.theme.indicator;

import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class RedLine<T extends Repaintable> extends LineTheme<T> {
	public RedLine(T chart) {
		super(chart);
		setLineColor(new Color(128, 0, 0, 160));
	}

}
