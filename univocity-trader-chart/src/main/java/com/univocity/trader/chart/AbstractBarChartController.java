package com.univocity.trader.chart;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;

import java.awt.*;

public abstract class AbstractBarChartController<C extends InteractiveChart> extends InteractiveChartController {

	@Label("Positive closing color")
	@ColorBound()
	private Color upColor = Color.GREEN;
	@Label("Positive selection color")
	@ColorBound()
	private Color upSelectionColor = Color.GRAY;


	@Label("Negative closing color")
	@ColorBound()
	private Color downColor = Color.RED;
	@Label("Negative selection color")
	@ColorBound()
	private Color downSelectionColor = Color.ORANGE;

	public AbstractBarChartController(C chart) {
		super(chart);
	}

	public Color getUpColor() {
		return upColor;
	}

	public void setUpColor(Color upColor) {
		this.upColor = upColor;
	}

	public Color getDownColor() {
		return downColor;
	}

	public void setDownColor(Color downColor) {
		this.downColor = downColor;
	}

	public Color getUpSelectionColor() {
		return upSelectionColor;
	}

	public void setUpSelectionColor(Color upSelectionColor) {
		this.upSelectionColor = upSelectionColor;
	}

	public Color getDownSelectionColor() {
		return downSelectionColor;
	}

	public void setDownSelectionColor(Color downSelectionColor) {
		this.downSelectionColor = downSelectionColor;
	}

	public Color getLineColor(Candle trade) {
		if (trade.isClosePositive()) {
			return getUpColor();
		} else {
			return getDownColor();
		}
	}

	public Color getLineSelectionColor(Candle trade) {
		if (trade.isClosePositive()) {
			return getUpSelectionColor();
		} else {
			return getDownSelectionColor();
		}
	}

}
