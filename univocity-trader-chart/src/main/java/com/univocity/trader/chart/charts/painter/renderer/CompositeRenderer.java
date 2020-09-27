package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public abstract class CompositeRenderer<T extends Theme> extends AbstractRenderer<T> {

	protected Renderer[] renderers;

	public CompositeRenderer(String description, T theme, Renderer[] renderers) {
		super(description, theme);
		this.renderers = renderers;
	}

	@Override
	public final void reset(int length) {
		for (int i = 0; i < renderers.length; i++) {
			renderers[i].reset(length);
		}
	}

	@Override
	public final void updateValue(Candle candle) {
		for (int i = 0; i < renderers.length; i++) {
			renderers[i].updateValue(candle);
		}
	}

	@Override
	public final void nextValue(Candle candle) {
		for (int i = 0; i < renderers.length; i++) {
			renderers[i].nextValue(candle);
		}
	}

	@Override
	public double getValueAt(int i) {
		return 0;
	}


	@Override
	public void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		for(int j = 0; j < renderers.length; j++){
			Renderer r = renderers[j];
			if(r.displayValue()){
				r.updateSelection(i, candle, candleLocation, chart, overlay, g, painter, headerLine);
			}
		}
	}
}