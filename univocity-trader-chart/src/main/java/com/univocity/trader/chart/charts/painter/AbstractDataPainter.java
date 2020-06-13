package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;
import java.util.function.*;

public abstract class AbstractDataPainter<T extends Theme> extends AbstractPainter<T> {

	private Renderer<?>[] renderers;

	private final Consumer<CandleHistory.UpdateType> historyUpdateConsumer;
	private final Painter<T> parent;
	private Candle prev;
	private final Runnable reset;
	private final Consumer<Candle> process;
	private final AreaPainter areaPainter;

	public AbstractDataPainter(Painter<T> parent, Runnable reset, Consumer<Candle> process) {
		super(parent.overlay());
		this.parent = parent;
		historyUpdateConsumer = this::processHistoryUpdate;
		this.reset = reset;
		this.process = process;
		this.areaPainter = parent instanceof AreaPainter ? (AreaPainter) parent : null;
	}

	@Override
	public final Rectangle bounds() {
		return parent.bounds();
	}

	protected abstract Renderer<?>[] createRenderers();

	protected final void reset() {
		if (this.renderers == null) {
			this.renderers = createRenderers();
			if (renderers == null) {
				throw new IllegalStateException("Renderers cannot be null");
			}
		}

		reset.run();
	}

	protected final void process(Candle candle) {
		process.accept(candle);
	}

	@Override
	public void paintOn(BasicChart<?> chart, Graphics2D g, int width) {
		for (int i = 0; i < chart.candleHistory.size(); i++) {
			for (int j = 0; j < renderers.length; j++) {
				renderers[j].paintNext(i, chart, g, areaPainter);
			}
		}
	}

	private void processHistoryUpdate(CandleHistory.UpdateType updateType) {
		if (updateType != CandleHistory.UpdateType.INCREMENT) {
			reset();
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
					renderers[j].nextValue();
				}
			}
		}

		Candle last = chart.candleHistory.getLast();
		if (prev != null && prev.openTime == last.openTime) {
			for (int j = 0; j < renderers.length; j++) {
				renderers[j].updateValue();
			}
		} else {
			for (int j = 0; j < renderers.length; j++) {
				renderers[j].nextValue();
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
