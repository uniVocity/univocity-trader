package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;

import java.awt.*;

public class SignalRenderer extends ObjectRenderer<Signal, AreaTheme> {

	public SignalRenderer(String description, AreaTheme theme, Indicator indicator) {
		super(description, theme, Signal[]::new, indicator::getSignal);
	}

	@Override
	public double getValueAt(int i) {
		return 0;
	}

	@Override
	public void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		if (i < objects.length && objects[i] != Signal.NEUTRAL) {
			headerLine.append(objects[i]);
		}
	}

	@Override
	protected void paintNext(int i, Signal signal, BasicChart<?> chart, Graphics2D g, AreaPainter areaPainter) {
		if (i < objects.length && signal != Signal.NEUTRAL) {
			Candle candle = chart.candleHistory.get(i);
			if (candle != null) {
				int x = chart.getXCoordinate(i);
				int y = areaPainter.bounds().y;

				Color background;
				if (signal.value > 0) {
					background = theme.getPositiveColor();
				} else {
					background = theme.getNegativeColor();
				}

				g.setColor(background);
				g.fillRect(x, y, chart.getBarWidth(), chart.getBarWidth());

				g.setStroke(new BasicStroke(1));
				g.drawRect(x, y, chart.getBarWidth(), chart.getBarWidth());
			}
		}
	}
}
