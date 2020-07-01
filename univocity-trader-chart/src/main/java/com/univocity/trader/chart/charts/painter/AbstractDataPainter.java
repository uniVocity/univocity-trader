package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;
import java.util.function.*;

public abstract class AbstractDataPainter<T extends Theme> extends AbstractPainter<T> {

	private final String description;
	private Renderer<?>[] renderers;

	private final Consumer<CandleHistory.UpdateType> historyUpdateConsumer;
	private final Painter<T> parent;
	private Candle prev;
	private final Runnable reset;
	private final Consumer<Candle> process;
	private final AreaPainter areaPainter;
	private final StringBuilder headerLine = new StringBuilder();
	private boolean resetting = false;

	public AbstractDataPainter(String description, Painter<T> parent, Runnable reset, Consumer<Candle> process) {
		super(parent.overlay());
		this.description = description;
		this.parent = parent;
		historyUpdateConsumer = this::processHistoryUpdate;
		this.reset = reset;
		this.process = process;
		this.areaPainter = parent instanceof AreaPainter ? (AreaPainter) parent : null;
	}

	@Override
	public String header() {
		return headerLine.toString();
	}

	@Override
	public final Rectangle bounds() {
		return parent.bounds();
	}

	protected abstract Renderer<?>[] createRenderers();

	protected final void reset() {
		if(resetting){
			return;
		}
		try{
			resetting = true;
			reset.run();

			if (this.renderers == null) {
				this.renderers = createRenderers();
				if (renderers == null) {
					throw new IllegalStateException("Renderers cannot be null");
				}
			}
		} finally {
			resetting = false;
		}

	}

	protected final void process(Candle candle) {
		process.accept(candle);
	}

	@Override
	public void paintOn(BasicChart<?> chart, Graphics2D g, int width, Overlay overlay) {
		for (int i = 0; i < chart.candleHistory.size(); i++) {
			for (int j = 0; j < renderers.length; j++) {
				renderers[j].paintNext(i, chart, overlay, g, areaPainter);
			}
		}

		Candle candle = chart.getCurrentCandle();
		Point candleLocation = chart.getCurrentCandleLocation();
		headerLine.setLength(0);
		if (candle != null && candleLocation != null) {
			if (description != null && !description.isBlank()) {
				headerLine.append(description);
			}

			int candleIndex = chart.candleHistory.indexOf(candle);
			if (candleIndex >= 0) {
				int len = headerLine.length();
				if (renderers.length > 0 && headerLine.length() > 0) {
					headerLine.append('[');
				}
				for (int i = 0; i < renderers.length; i++) {
					if (renderers[i].displayValue()) {
						renderers[i].updateSelection(candleIndex, candle, candleLocation, chart, overlay, g, areaPainter, headerLine);
					}
				}

				if (headerLine.length() == len + 1) { //nothing appended by renderers, remove the '['
					headerLine.deleteCharAt(len);
				} else {
					if (renderers.length > 0 && headerLine.length() > 0) {
						headerLine.append(']');
					}
				}
			}
		}
	}

	private void processHistoryUpdate(CandleHistory.UpdateType updateType) {
		if (chart == null) { //painter uninstalled.
			return;
		}
		if (updateType != CandleHistory.UpdateType.INCREMENT) {
			reset();
			if (chart == null) {
				return;
			}
			final int historySize = chart.candleHistory.size();
			for (int j = 0; j < renderers.length; j++) {
				renderers[j].reset(historySize);
			}
			for (int i = 0; i < historySize - 1; i++) {
				Candle candle = chart.candleHistory.get(i);
				if (candle == null) {
					return;
				}
				process(candle);
				for (int j = 0; j < renderers.length; j++) {
					renderers[j].nextValue(candle);
				}
			}
		}

		Candle last = chart.candleHistory.getLast();
		if (prev != null && last != null && prev.openTime == last.openTime) {
			for (int j = 0; j < renderers.length; j++) {
				renderers[j].updateValue(last);
			}
		} else {
			for (int j = 0; j < renderers.length; j++) {
				renderers[j].nextValue(last);
			}
		}
		prev = last;

		invokeRepaint();
	}

	@Override
	public void install(BasicChart<?> chart) {
		super.install(chart);
		chart.candleHistory.addDataUpdateListener(historyUpdateConsumer);
		historyUpdateConsumer.accept(CandleHistory.UpdateType.NEW_HISTORY);
	}

	@Override
	public void uninstall(BasicChart<?> chart) {
		super.uninstall(chart);
		chart.candleHistory.removeDataUpdateListener(historyUpdateConsumer);
	}


}
