package com.univocity.trader.chart.charts.theme;

import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public class AbstractTheme<T extends Repaintable> extends Theme {

	private Color headerColor = new Color(128, 128, 128, 150);
	private Color selectedHeaderColor = new Color(0, 0, 0, 180);

	private Font hoveredHeaderFont = new Font("Arial", Font.PLAIN, 10);
	private Font selectedHeaderFont = new Font("Arial", Font.BOLD, 10);

	private Stroke normalStroke = new BasicStroke(1);

	@Label("Line stroke")
	@SpinnerBound(maximum = 10)
	private int stroke = 1;

	protected T chart;

	public AbstractTheme(T chart) {
		this.chart = chart;
	}

	public T getChart() {
		return chart;
	}

	@Override
	public final void invokeRepaint() {
		if (chart != null) {
			chart.invokeRepaint();
		}
	}

	public int getStroke() {
		return stroke;
	}

	public void setStroke(int stroke) {
		this.stroke = stroke;
		this.setNormalStroke(new BasicStroke(stroke));
	}


	public Stroke getNormalStroke() {
		return normalStroke;
	}

	public void setNormalStroke(Stroke normalStroke) {
		this.normalStroke = normalStroke;
	}

	public Color getHeaderColor() {
		return headerColor;
	}

	public void setHeaderColor(Color headerColor) {
		this.headerColor = headerColor;
	}

	public Color getSelectedHeaderColor() {
		return selectedHeaderColor;
	}

	public void setSelectedHeaderColor(Color selectedHeaderColor) {
		this.selectedHeaderColor = selectedHeaderColor;
	}

	public Font getHoveredHeaderFont() {
		return hoveredHeaderFont;
	}

	public Font getSelectedHeaderFont() {
		return selectedHeaderFont;
	}
}
