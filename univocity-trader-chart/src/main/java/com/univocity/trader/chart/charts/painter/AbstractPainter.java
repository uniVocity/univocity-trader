package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

public abstract class AbstractPainter<T extends Theme> implements Painter<T> {

	final Z z;
	protected BasicChart<?> chart;
	private T theme;

	public AbstractPainter(Z z){
		this.z = z;
	}

	@Override
	public final Z getZ() {
		return z;
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

	public final T getTheme(){
		if(theme == null){
			theme = newTheme();
		}
		return theme;
	}

	protected abstract T newTheme();
}
