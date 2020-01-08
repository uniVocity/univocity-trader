package com.univocity.trader.chart;

import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;

import java.awt.*;

public class LineChartController extends InteractiveChartController {

	
	private Stroke normalStroke = new BasicStroke(1);
	
	@Label("Line stroke")
	@SpinnerBound(maximum = 10)
	private int stroke = 1;

	@Label("Marker width")
	@SpinnerBound(maximum = 20)
	private int markerWidth = 8;

	@Label("Marker color")
	@ColorBound
	private Color markerColor = Color.BLUE;

	@Label("Line color")
	@ColorBound
	private Color lineColor = Color.BLACK;

	public LineChartController(LineChart chart) {
		super(chart);
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

	public Stroke getNormalStroke() {
		return normalStroke;
	}

	public void setNormalStroke(Stroke normalStroke) {
		this.normalStroke = normalStroke;
	}

	public int getStroke() {
		return stroke;
	}

	public void setStroke(int stroke) {
		this.stroke = stroke;
		this.setNormalStroke(new BasicStroke(stroke));
	}
	
}
