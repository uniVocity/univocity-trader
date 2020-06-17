package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.chart.dynamic.*;

public abstract class CompositeRenderer<T extends Theme> extends AbstractRenderer<T> {

	protected Renderer[] renderers;

	public CompositeRenderer(String description, T theme, Renderer[] renderers) {
		super(description, theme);
		setConstant(true);
		this.renderers = renderers;
	}

	@Override
	public final void reset(int length) {
		for (int i = 0; i < renderers.length; i++) {
			renderers[i].reset(length);
		}
	}

	@Override
	public final void updateValue() {
		for (int i = 0; i < renderers.length; i++) {
			renderers[i].updateValue();
		}
	}

	@Override
	public final void nextValue() {
		for (int i = 0; i < renderers.length; i++) {
			renderers[i].nextValue();
		}
	}

	@Override
	public double getValueAt(int i) {
		return 0;
	}

}