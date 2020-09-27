package com.univocity.trader.chart.charts.theme;

import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;

public class BoundaryLineTheme<T extends Repaintable> extends LineTheme<T> {

	public BoundaryLineTheme(T chart) {
		super(chart);
		setLineColor(new Color(128, 128, 128, 160));
		setNormalStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8}, 0));
	}

}
