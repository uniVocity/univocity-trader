package com.univocity.trader.chart.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.charts.theme.*;
import com.univocity.trader.chart.dynamic.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

public class VisualIndicator implements Painter<CompositeTheme> {

	private static final LineRenderer[] EMPTY = new LineRenderer[0];

	private final Supplier<Indicator> indicatorSupplier;
	private Aggregator aggregator;
	private Indicator indicator;
	BasicChart<?> chart;
	private final Supplier<TimeInterval> interval;

	private Painter<CompositeTheme> indicatorPainter;

	public VisualIndicator(Supplier<TimeInterval> interval, Supplier<Indicator> indicator) {
		this.indicatorSupplier = indicator;
		this.interval = interval;
	}

	private LineRenderer[] createRenderers() {
		List<Renderer<?>> out = new ArrayList<>();

		for (Method m : getIndicator().getClass().getMethods()) {
			if (m.getReturnType() == double.class && m.getParameterCount() == 0) {
				out.add(createRenderer(m));
			}
		}

		return out.toArray(LineRenderer[]::new);
	}

	private LineRenderer createRenderer(Method m) {
		return new LineRenderer(new LineTheme<>(this), () -> invoke(m));
	}

	private double invoke(Method m) {
		try {
			return (double) m.invoke(getIndicator());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void reset() {
		aggregator = new Aggregator("").getInstance(interval.get());
		getIndicator().initialize(aggregator);
	}

	private Painter<CompositeTheme> getIndicatorPainter() {
		if (indicatorPainter == null) {
			indicatorPainter = new LinePainter(Z.FRONT, this::reset, this::process, createRenderers());
		}
		return indicatorPainter;
	}

	private void process(Candle candle) {
		aggregator.aggregate(candle);
		getIndicator().accumulate(candle);
	}


	@Override
	public Z getZ() {
		return getIndicatorPainter().getZ();
	}

	@Override
	public CompositeTheme getTheme() {
		return getIndicatorPainter().getTheme();
	}

	@Override
	public void paintOn(BasicChart<?> chart, Graphics2D g, int width) {
		getIndicatorPainter().paintOn(chart, g, width);
	}

	@Override
	public void install(BasicChart<?> chart) {
		getIndicatorPainter().install(chart);
	}

	@Override
	public void uninstall(BasicChart<?> chart) {
		getIndicatorPainter().uninstall(chart);
	}

	@Override
	public void invokeRepaint() {
		getIndicatorPainter().invokeRepaint();
	}

	private Indicator getIndicator() {
		if (indicator == null) {
			indicator = indicatorSupplier.get();
		}
		return indicator;
	}
}
