package com.univocity.trader.chart.charts.painter.renderer;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;

import java.awt.*;

public class SignalRenderer extends ObjectRenderer<String, AreaTheme> {

	private static String NEUTRAL = Signal.NEUTRAL.toString().intern();

	public SignalRenderer(String description, AreaTheme theme, Indicator indicator) {
		super(description, theme, String[]::new, (c) -> generateSignal(indicator, c));
		displayValue(false);
	}


	@Override
	public double getValueAt(int i) {
		return 0;
	}

	@Override
	public Color getColorAt(int i) {
		return null;
	}

	private static String generateSignal(Indicator indicator, Candle candle) {
		Signal signal = indicator.getSignal(candle);
		if (signal == null) {
			signal = Signal.NEUTRAL;
		}

		String out = signal.toString();
		if (signal != Signal.NEUTRAL) {
			String description = indicator.signalDescription();
			if (description != null && !description.isBlank()) {
				out = out + ": " + description;
			}
		}

		return out.intern();
	}

	@Override
	public void updateSelection(int i, Candle candle, Point candleLocation, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter painter, StringBuilder headerLine) {
		if (i < objects.length && objects[i] != null) {
			if (g != null && painter != null && painter.bounds() != null) {
				paintNext(i, objects[i], chart, overlay, g, painter);
			}
			headerLine.append(objects[i]);
		}
	}

	@Override
	protected void paintNext(int i, String signal, BasicChart<?> chart, Painter.Overlay overlay, Graphics2D g, AreaPainter areaPainter) {
		if (i < objects.length && signal != NEUTRAL) {
			Candle candle = chart.candleHistory.get(i);
			if (candle != null) {
				int x = chart.getXCoordinate(i);
				int y = areaPainter.bounds().y;

				char ch = signal.charAt(0);
				switch (ch) {
					case 'B': //buy
					case 'U': //undervalued
						g.setColor(theme.getPositiveColor());
						break;
					case 'S': //sell
					case 'O': //overvalued
						g.setColor(theme.getNegativeColor());

				}

				int off = (int) Math.round(chart.getBarWidth() / 2.0);
				g.fillRect(x - off, y, chart.getBarWidth(), chart.getBarWidth());

				g.setStroke(new BasicStroke(1));
				g.drawRect(x - off, y, chart.getBarWidth(), chart.getBarWidth());
			}
		}
	}
}
