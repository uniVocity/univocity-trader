package com.univocity.trader.chart.charts.scrolling;

import com.univocity.trader.candles.*;

import java.util.*;

public interface IntervalListener extends EventListener {

	void intervalUpdated(Candle from, Candle to);
}