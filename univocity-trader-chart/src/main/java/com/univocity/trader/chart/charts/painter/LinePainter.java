package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;
import java.util.function.*;

public class LinePainter extends AbstractDataPainter<CompositeTheme> {

	private final LineRenderer[] renderers;

	public LinePainter(Painter<CompositeTheme> parent, Runnable reset, Consumer<Candle> process, LineRenderer... renderers) {
		super(parent, reset, process);
		this.renderers = renderers;
	}

	@Override
	protected CompositeTheme newTheme() {
		return new CompositeTheme(this, renderers);
	}

	@Override
	protected LineRenderer[] createRenderers() {
		return renderers;
	}
}
