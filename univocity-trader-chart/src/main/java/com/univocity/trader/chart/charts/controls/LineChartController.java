package com.univocity.trader.chart.charts.controls;

import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.*;

import java.awt.*;

public class LineChartController extends InteractiveChartController {

	@Label("Marker width")
	@SpinnerBound(maximum = 20)
	private int markerWidth = 8;

	@Label("Marker color")
	@ColorBound
	private Color markerColor = Color.BLUE;

	@Label("Line color")
	@ColorBound
	private Color lineColor = Color.WHITE;

	public LineChartController(LineChart chart) {
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

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

}
