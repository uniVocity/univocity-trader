package com.univocity.trader.chart.charts.controls;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.*;

import java.awt.*;

public abstract class AbstractFilledBarChartController<C extends InteractiveChart<?>> extends AbstractBarChartController<C> {

	@CheckBoxBound("Fill positive candles")
	private boolean positiveClosingFilled = true;

	@CheckBoxBound("Fill negative candles")
	private boolean negativeClosingFilled = true;

	@Label("Positive fill color")
	@ColorBound()
	private Color upFillColor = Color.GREEN;
	@Label("Positive selection fill color")
	@ColorBound()
	private Color upSelectionFillColor = Color.GRAY;


	@Label("Negative fill color")
	@ColorBound()
	private Color downFillColor = Color.RED;
	@Label("Negative selection fill color")
	@ColorBound()
	private Color downSelectionFillColor = Color.ORANGE;

	public AbstractFilledBarChartController(C chart) {
		super(chart);
	}

	public boolean getPositiveClosingFilled() {
		return positiveClosingFilled;
	}

	public void setPositiveClosingFilled(boolean isPositiveClosingFilled) {
		this.positiveClosingFilled = isPositiveClosingFilled;
	}

	public boolean getNegativeClosingFilled() {
		return negativeClosingFilled;
	}

	public void setNegativeClosingFilled(boolean isNegativeClosingFilled) {
		this.negativeClosingFilled = isNegativeClosingFilled;
	}

	public Color getUpFillColor() {
		return upFillColor;
	}

	public void setUpFillColor(Color upFillColor) {
		this.upFillColor = upFillColor;
	}

	public Color getUpSelectionFillColor() {
		return upSelectionFillColor;
	}

	public void setUpSelectionFillColor(Color upSelectionFillColor) {
		this.upSelectionFillColor = upSelectionFillColor;
	}

	public Color getDownFillColor() {
		return downFillColor;
	}

	public void setDownFillColor(Color downFillColor) {
		this.downFillColor = downFillColor;
	}

	public Color getDownSelectionFillColor() {
		return downSelectionFillColor;
	}

	public void setDownSelectionFillColor(Color downSelectionFillColor) {
		this.downSelectionFillColor = downSelectionFillColor;
	}

	public Color getFillColor(Candle candle) {
		if (candle.isClosePositive()) {
			return getUpFillColor();
		} else {
			return getDownFillColor();
		}
	}

	public Color getSelectionFillColor(Candle selected) {
		if (selected.isClosePositive()) {
			return getUpSelectionFillColor();
		} else {
			return getDownSelectionFillColor();
		}
	}
}
