package com.univocity.trader.chart.charts.theme;


import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;

public class AreaTheme extends AbstractTheme<Repaintable> {

	@Label("Positive color")
	@ColorBound()
	private Color positiveColor = new Color(0, 153, 51, 128);

	@Label("Negative color")
	@ColorBound()
	private Color negativeColor = new Color(205, 0, 51, 128);

	public AreaTheme(Repaintable chart) {
		super(chart);
	}

	public Color getPositiveColor() {
		return positiveColor;
	}

	public void setPositiveColor(Color positiveColor) {
		this.positiveColor = positiveColor;
	}

	public Color getNegativeColor() {
		return negativeColor;
	}

	public void setNegativeColor(Color negativeColor) {
		this.negativeColor = negativeColor;
	}

	public Color getLineColor(double value) {
		if (value >= 0) {
			return getPositiveColor();
		} else {
			return getNegativeColor();
		}
	}
}
