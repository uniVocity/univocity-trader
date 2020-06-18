package com.univocity.trader.chart.charts.ruler;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.indicators.*;
import org.apache.commons.lang3.*;

import java.awt.*;
import java.text.*;
import java.util.*;

import static com.univocity.trader.chart.charts.ruler.DrawingProfile.Profile.*;


public class ValueRuler extends Ruler<ValueRulerTheme> {

	private Point gradientStart = new Point(0, 0);
	private Point gradientEnd = new Point(0, 0);
	private static final Color glassGray = new Color(222, 222, 222, 180);
	private static final Color glassWhite = new Color(255, 255, 255, 128);

	private int refY1, refY2, refY3;

	public ValueRuler(BasicChart<?> chart) {
		super(chart);
	}

	private int getRightValueTagSpacing() {
		return this.theme().getRightValueTagSpacing();
	}

	private void drawGlass(BasicChart<?> chart, Graphics2D g) {
		int width = insets.right + getRightValueTagSpacing();
		int start = chart.getBoundaryRight() - width;
		int end = insets.right + width;

		gradientStart.x = chart.getWidth();
		gradientStart.y = chart.getHeight() + 250;
		gradientEnd.x = chart.getWidth();
		gradientEnd.y = -250;


		g.setPaint(new GradientPaint(gradientStart, glassGray, gradientEnd, glassWhite));
		g.fillRect(start, 0, end, chart.getHeight());
	}

	protected void drawBackground(BasicChart<?> chart, Graphics2D g, int width) {
		setProfile(DEFAULT);

		drawGlass(chart, g);

		final double yIncrement = getFontHeight();

		int height = chart.getAvailableHeight();
		int y = height - chart.getYCoordinate(chart.getMaximum());

		int insetRight = 0;
		while (y > 0) {
			String tag = getValueFormat().format(chart.getValueAtY(y));
			int tagWidth = this.theme().getMaxStringWidth(tag, g);

			insetRight = Math.max(insetRight, tagWidth);

			int yy = height - y;
			text(g);
			g.drawString(tag, chart.getBoundaryRight() - tagWidth - getRightValueTagSpacing(), yy + (getFontHeight() / 2));
			y -= yIncrement;
		}

		insets.right = insetRight;
	}

	private int getMinimumWidth() {
		return this.theme().getMinimumWidth();
	}

	public int getRulerWidth() {
		return getMinimumWidth();
	}

	private void drawLine(int y, Graphics2D g, int length, int width) {
		drawing(g);
		g.drawLine(width - length, y, width, y);
	}

	protected void drawSelection(BasicChart<?> chart, Graphics2D g, int width, Candle candle, Point location) {
		setProfile(SELECTION);
		refY1 = drawPrices(chart, g, location, candle, chart.getCentralValue(candle), true, -1, false);

		setProfile(HIGHLIGHT);
		refY2 = drawPrices(chart, g, location, candle, chart.getHighestPlottedValue(candle), false, refY1, true);
		refY3 = drawPrices(chart, g, location, candle, chart.getLowestPlottedValue(candle), false, refY1, false);

		for (Painter<?> underlay : chart.underlays()) {
			if (underlay instanceof VisualIndicator) {
				VisualIndicator p = (VisualIndicator) underlay;
				double[] values = p.getCurrentSelectionValues(chart.candleHistory.indexOf(candle));
				if (values != null && values.length > 0) {
					setProfile(HIGHLIGHT);
					Arrays.sort(values);
					ArrayUtils.reverse(values);

					if (values.length > 2) {
						int middle = values.length / 2;
						refY1 = drawValue(values[middle], p, chart, g, location, candle, -1, true, true);
						refY2 = drawValue(values[0], p, chart, g, location, candle, refY1, true, false);
						refY3 = drawValue(values[values.length - 1], p, chart, g, location, candle, refY1, false, false);
					} else if (values.length == 2) {
						refY1 = drawValue(values[0], p, chart, g, location, candle, -1, true, false);
						refY2 = drawValue(values[1], p, chart, g, location, candle, refY1, false, false);
					} else {
						refY1 = drawValue(values[0], p, chart, g, location, candle, -1, false, true);
					}
				}
			}
		}
	}

	private int drawValue(double value, VisualIndicator p, BasicChart<?> chart, Graphics2D g, Point location, Candle candle, int refY, boolean drawAboveRef, boolean inMiddle) {
		Rectangle bounds = p.bounds();
		int y = bounds.y + p.getYCoordinate(value, bounds.height);
		refY = drawValues(chart, y, bounds.y + bounds.height, g, location, candle, value, false, refY, drawAboveRef, inMiddle);
		return refY;
	}

	@Override
	protected void highlightMousePosition(BasicChart<?> chart, Graphics2D g, int width) {
		Point mousePosition = chart.getCurrentMousePosition();
		if (mousePosition != null && chart.isMouseDraggingCursor()) {
			setProfile(HIGHLIGHT);
			double valueAtMouseHeight = chart.getValueAtY(chart.getAvailableHeight() - mousePosition.y);
			drawPrices(chart, g, mousePosition, null, valueAtMouseHeight, true, -1, false);
		}
	}

	private boolean collides(int stringY, int refY, int fontHeight) {
		int fh = fontHeight / 2;
		return (stringY + fh >= refY - fh) && (stringY - fh <= refY + fh);
	}

	private int drawPrices(BasicChart<?> chart, Graphics2D g, Point location, Candle candle, double value, boolean drawInBox, int refY, boolean drawAboveRef) {
		int y = chart.getYCoordinate(value);
		return drawValues(chart, y, chart.getAvailableHeight(), g, location, candle, value, drawInBox, refY, drawAboveRef, false);
	}

	private int drawValues(BasicChart<?> chart, int y, int height, Graphics2D g, Point location, Candle candle, double value, boolean drawInBox, int refY, boolean drawAboveRef, boolean inMiddle) {
		final int fontHeight = this.theme().getFontHeight();
		int stringY = this.theme().centralizeYToFontHeight(y);

		if (stringY + fontHeight > height) {
			stringY = height - fontHeight;
		} else if (stringY < 0) {
			stringY = 0;
		}

		if (refY >= 0 && collides(stringY, refY, fontHeight)) {
			stringY = stringY + (drawAboveRef ? -fontHeight : fontHeight);
		} else if (candle == null) {
			if (collides(stringY, refY1, fontHeight) || collides(stringY, refY2, fontHeight) || collides(stringY, refY3, fontHeight)) {
				return -1;
			}
		}

		String tag = format(value);
		int tagWidth = this.theme().getMaxStringWidth(tag, g);

		int x = chart.getBoundaryRight() - tagWidth - this.theme().getRightValueTagSpacing();
		if (drawInBox || inMiddle) {
			if (drawInBox) {
				drawStringInBox(x, stringY, chart.getWidth(), tag, g, 1, candle == null ? getBackgroundColor() : candle.isGreen() ? getProfitBackground() : getLossBackground());
			} else {
				drawString(x, stringY, tag, g, 1);
			}
		} else {
			if (drawAboveRef && refY != -1) {
				setProfile(SELECTION);
				drawing(g);
				g.drawLine(x, stringY, x + chart.getWidth(), stringY);
			}
			setProfile(HIGHLIGHT);
			text(g);
			drawString(x, stringY, tag, g, 1);
			if (!drawAboveRef && refY != -1) {
				setProfile(SELECTION);
				drawing(g);
				g.drawLine(x, stringY + fontHeight, x + chart.getWidth(), stringY + fontHeight);
			}
		}

		if ((!drawInBox || candle == null)) {
			setProfile(HIGHLIGHT);
			drawing(g);
			drawLineToPrice(g, fontHeight, location, x, y, stringY);
		}

		return stringY;
	}

	private void drawLineToPrice(Graphics2D g, int fontHeight, Point location, int x, int y, int stringY) {
		drawing(g);
		int heightAdjust = (fontHeight / 2);
		g.drawLine(location.x, y, x, stringY + heightAdjust);
	}

	private void drawGrid(int y, Graphics2D g, int width) {
		if (isShowingGrid()) {
			g.setColor(getGridColor());
			g.drawLine(0, y, width, y);
		}
	}

	@Override
	protected ValueRulerTheme newTheme() {
		return new ValueRulerTheme(this);
	}

	protected String format(double value) {
		return getValueFormat().format(value);
	}

	protected Format getValueFormat() {
		return Candle.PRICE_FORMAT.get();
	}

	@Override
	public Overlay overlay() {
		return Overlay.FRONT;
	}
}
