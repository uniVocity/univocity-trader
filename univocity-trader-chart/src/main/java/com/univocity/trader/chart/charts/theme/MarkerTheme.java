package com.univocity.trader.chart.charts.theme;

import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;

public class MarkerTheme<T extends Repaintable> extends PainterTheme<T> {

	@Label("Marker width")
	@SpinnerBound(maximum = 20)
	private int markerWidth = 5;

	@Label("Marker color")
	@ColorBound
	private Color markerColor = Color.BLUE;

	public MarkerTheme(T chart) {
		super(chart);
		setSpaceBetweenBars(0);
	}

	public int getMarkerWidth() {
		return markerWidth;
	}

	public void setMarkerWidth(int markerWidth) {
		this.markerWidth = markerWidth;
	}

	public Color getMarkerColor() {
		return markerColor;
	}

	public void setMarkerColor(Color markerColor) {
		this.markerColor = markerColor;
	}
}
