package com.univocity.trader.chart.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

public class VisualIndicator implements Painter<VisualIndicatorController> {

	private Supplier<Indicator> indicatorSupplier;
	private Indicator indicator;
	private Z z = Z.FRONT;
	BasicChart<?> chart;
	private Supplier<TimeInterval> interval;

	private final List<ValuePlotter> plotters = new ArrayList<>();

	private final Consumer<CandleHistory.UpdateType> historyUpdateConsumer = t -> {
		indicator = this.indicatorSupplier.get();
		Aggregator aggregator = new Aggregator("").getInstance(interval.get());
		indicator.initialize(aggregator);

		final int historySize = chart.candleHistory.size();
		for (int j = 0; j < plotters.size(); j++) {
			plotters.get(j).reset(historySize);
		}

		for (int i = 0; i < historySize; i++) {
			Candle candle = chart.candleHistory.get(i);
			if(candle == null){
				return;
			}
			aggregator.aggregate(candle);
			indicator.accumulate(candle);
			for (int j = 0; j < plotters.size(); j++) {
				plotters.get(j).nextValue();
			}
		}

		invokeRepaint();
	};

	public VisualIndicator(Supplier<TimeInterval> interval, Supplier<Indicator> indicator) {
		this.indicatorSupplier = indicator;
		this.interval = interval;

		createPlotters();
	}

	@Override
	public Z getZ() {
		return z;
	}

	@Override
	public void paintOn(BasicChart<?> chart, Graphics2D g, int width) {
		for (int i = 0; i < chart.candleHistory.size(); i++) {
			for (int j = 0; j < plotters.size(); j++) {
				plotters.get(j).plotNext(i, chart, g, width);
			}
		}
	}

	@Override
	public VisualIndicatorController getController() {
		return new VisualIndicatorController(this);
	}

	@Override
	public void install(BasicChart<?> chart) {
		this.chart = chart;
		chart.candleHistory.addDataUpdateListener(historyUpdateConsumer);
		historyUpdateConsumer.accept(CandleHistory.UpdateType.NEW_HISTORY);
	}

	@Override
	public void uninstall(BasicChart<?> chart) {
		chart.candleHistory.removeDataUpdateListener(historyUpdateConsumer);
		this.chart = null;
	}

	@Override
	public void invokeRepaint() {
		if (this.chart != null) {
			chart.invokeRepaint();
		}
	}

	private Indicator getIndicator() {
		return indicator;
	}

	private void createPlotters() {
		ValuePlotter plotter = new ValuePlotter(this, () -> getIndicator().getValue());
		plotters.add(plotter);
	}
}
