package com.univocity.trader.chart.charts.theme;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;

public abstract class AbstractBarTheme<C extends Repaintable> extends PainterTheme<C> {

	@Label("Positive closing color")
	@ColorBound()
	private Color upColor = new Color(0, 153, 51);
	@Label("Positive selection color")
	@ColorBound()
	private Color upSelectionColor = new Color(220, 220, 255);


	@Label("Negative closing color")
	@ColorBound()
	private Color downColor = new Color(205, 0, 51);
	@Label("Negative selection color")
	@ColorBound()
	private Color downSelectionColor = new Color(152, 102, 0);

	public AbstractBarTheme(C chart) {
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

	public Color getSelectionLineColor(Candle trade) {
		if (trade.isClosePositive()) {
			return getUpSelectionColor();
		} else {
			return getDownSelectionColor();
		}
	}

}
