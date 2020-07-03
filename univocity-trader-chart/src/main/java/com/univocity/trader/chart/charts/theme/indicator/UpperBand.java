package com.univocity.trader.chart.charts.theme.indicator;

import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;

public class UpperBand<T extends Repaintable> extends LineTheme<T> {
	public UpperBand(T chart) {
		super(chart);
		setLineColor(new Color(128, 128, 255, 160));
	}

}
