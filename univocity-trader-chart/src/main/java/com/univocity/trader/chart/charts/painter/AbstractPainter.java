package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public abstract class AbstractPainter<T extends Theme> implements Painter<T> {

	final Overlay overlay;
	protected BasicChart<?> chart;
	private T theme;

	public AbstractPainter(Overlay overlay) {
		this.overlay = overlay;
	}

	@Override
	public final Overlay overlay() {
		return overlay;
	}

	@Override
	public void install(BasicChart<?> chart) {
		this.chart = chart;
	}

	@Override
	public void uninstall(BasicChart<?> chart) {
		this.chart = null;
	}

	@Override
	public void invokeRepaint() {
		if (this.chart != null) {
			chart.invokeRepaint();
		}
	}

	public final T theme() {
		if (theme == null) {
			theme = newTheme();
		}
		return theme;
	}

	protected abstract T newTheme();
}
