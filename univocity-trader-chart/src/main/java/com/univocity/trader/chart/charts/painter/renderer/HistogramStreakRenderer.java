package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.util.function.*;

public class HistogramStreakRenderer extends HistogramRenderer {

	public HistogramStreakRenderer(String description, HistogramTheme<?> theme, DoubleSupplier valueSupplier) {
		super(description, theme, valueSupplier);
	}

	protected Color getLineColor(Color currentLineColor, double previousValue, double currentValue) {
		return currentValue > previousValue ? theme.getUpColor() : theme.getDownColor();
	}

	protected Color getFillColor(Color currentFillColor, double previousValue, double currentValue) {
		return currentValue > previousValue ? theme.getUpFillColor() : theme.getDownFillColor();
	}

	protected Color getSelectionLineColor(Color currentLineColor, double previousValue, double currentValue) {
		return currentValue > previousValue ? theme.getUpSelectionColor() : theme.getDownSelectionColor();
	}

	protected Color getSelectionFillColor(Color currentFillColor, double previousValue, double currentValue) {
		return currentValue > previousValue ? theme.getUpSelectionFillColor() : theme.getDownSelectionFillColor();
	}
}
