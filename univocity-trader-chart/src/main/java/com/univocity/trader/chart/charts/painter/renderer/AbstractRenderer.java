package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.dynamic.*;

public abstract class AbstractRenderer<T extends Theme> implements Renderer<T> {

	private final String description;
	protected final T theme;
	private boolean constant = false;


	public AbstractRenderer(String description, T theme) {
		this.description = description;
		this.theme = theme;
	}

	@Override
	public final String description() {
		return description;
	}


	@Override
	public final T getTheme() {
		return theme;
	}


	@Override
	public final boolean constant() {
		return constant;
	}

	public final void setConstant(boolean constant) {
		this.constant = constant;
	}
}
