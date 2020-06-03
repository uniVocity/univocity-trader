package com.univocity.trader.chart.indicators;

public interface IndicatorListener {

	void indicatorUpdated(boolean preview, VisualIndicator previous, VisualIndicator current);

}
