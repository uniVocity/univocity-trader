package com.univocity.trader.chart.charts.ruler;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;

import java.awt.*;
import java.text.*;

import static com.univocity.trader.chart.charts.ruler.DrawingProfile.Profile.*;


public class ValueRuler extends Ruler<ValueRulerController> {

	public ValueRuler(BasicChart<?> chart) {
		super(chart);
	}

	private int getRightValueTagSpacing() {
		return getController().getRightValueTagSpacing();
	}

	private int getFontHeight() {
		return getController().getFontHeight();
	}

	protected void drawBackground(Graphics2D g) {
		getController().setProfile(DEFAULT);
		getController().updateFontSize(g);

		final double yIncrement = getController().getFontHeight();

		int y = chart.getHeight() - chart.getYCoordinate(chart.getMaximum());
		getController().text(g);

		while (y > 0) {
			String tag = getValueFormat().format(chart.getValueAtY(y));
			int tagWidth = getController().getMaxStringWidth(tag, g);

			int yy = chart.getHeight() - y;

			g.drawString(tag, chart.getWidth() - tagWidth - getRightValueTagSpacing(), yy + (getFontHeight() / 2));
			drawLine(yy, g);

			y -= yIncrement;
		}

		getController().setProfile(DEFAULT);
		getController().drawing(g);
	}

	private int getMinimumWidth() {
		return getController().getMinimumWidth();
	}

	public int getRulerWidth() {
		return getMinimumWidth();
	}

	private void drawLine(int y, Graphics2D g) {
		getController().drawing(g);
		g.drawLine(0, y, getController().getLineWidth(), y);
	}

	protected void drawSelection(Graphics2D g, Candle candle, Point location) {
		getController().setProfile(SELECTION);

		final int y = chart.getYCoordinate(chart.getCentralValue(candle));
		final int fontHeight = getController().getFontHeight();
		int stringY = getController().centralizeYToFontHeight(y);

		if (stringY + fontHeight > chart.getHeight()) {
			stringY = chart.getHeight() - fontHeight;
		} else if (stringY < 0) {
			stringY = 0;
		}
		drawGrid(y, g);
		String tag = readFieldFormatted(candle);
		int tagWidth = getController().getMaxStringWidth(tag, g);
		getController().drawStringInBox(chart.getWidth() - tagWidth - getController().getRightValueTagSpacing(), stringY, chart.getWidth(), tag, g, 1);

		drawLine(y, g);

	}

	private boolean isShowingGrid() {
		return getController().isShowingGrid();
	}

	private Color getGridColor() {
		return getController().getGridColor();
	}

	private void drawGrid(int y, Graphics2D g) {
		if (isShowingGrid()) {
			g.setColor(getGridColor());
			final int chartWidth = chart.getWidth();

			g.drawLine(0, y, chartWidth, y);
		}
	}

	@Override
	protected ValueRulerController newController() {
		return new ValueRulerController(this);
	}

	@Override
	protected Format getValueFormat() {
		return Candle.PRICE_FORMAT.get();
	}

	@Override
	public Z getZ() {
		return Z.BACK;
	}
}
