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
	private double currentPrice;

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

		final int height = chart.getAvailableHeight();
		int y = height - chart.getYCoordinate(chart.getMaximum());

		int insetRight = 0;

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		while (y > 0) {
			double value = chart.getValueAtY(y);
			if(value < min){
				min = value;
			}
			if(value > max){
				max = value;
			}

			insetRight = drawValue(chart, g, value, insetRight, height, y, false);
			y -= yIncrement;
		}

		if(currentPrice > 0.0){
			if(currentPrice < min){
				y = height - chart.getYCoordinate(chart.getMaximum());
			} else if(currentPrice > max){
				y = 0;
			} else {
				y = chart.getYCoordinate(currentPrice);
			}

			insetRight = drawValue(chart, g, currentPrice, insetRight, height,  y - getFontHeight() / 2, true);

			g.setColor(getColorForCandle(chart.candleHistory.getLast()));
			g.fillOval(chart.getBoundaryRight() - insetRight - 3, y - 3, 4, 4);

//			final int fontHeight = this.theme().getFontHeight();
//			int stringY = this.theme().centralizeYToFontHeight(y);
//			Point location = new Point(chart.getBoundaryRight() - insetRight, y);
//
//			setProfile(HIGHLIGHT);
//			drawing(g);
//			drawLineToPrice(g, fontHeight, location, 0, y, stringY, null);
		}

		insets.right = insetRight;
	}

	private int drawValue(BasicChart<?> chart, Graphics2D g, double value, int insetRight, int height, int y, boolean highlight){
		String tag = getValueFormat().format(value);
		int tagWidth = this.theme().getMaxStringWidth(tag, g);

		insetRight = Math.max(insetRight, tagWidth);

		int yy = height - y;
		text(g);

		if(highlight){
			int x = chart.getBoundaryRight() - tagWidth - this.theme().getRightValueTagSpacing();
			drawStringInBox(x, y, tagWidth, tag, g, 2, chart.theme().getBackgroundColor());
		} else {
			g.drawString(tag, chart.getBoundaryRight() - tagWidth - getRightValueTagSpacing(), yy + (getFontHeight() / 2));
		}

		return insetRight;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
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

	private void reverseSort(double values[], Color[] colors){
		for(int i = 0; i < values.length; i++){
			for(int j = i + 1; j < values.length; j++){
				double vi = values[i];
				double vj = values[j];

				Color ci = colors[i];
				Color cj = colors[j];

				if(vj > vi){
					values[j] = vi;
					values[i] = vj;
					colors[j] = ci;
					colors[i] = cj;
				}
			}
		}
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
				int candleIndex = chart.candleHistory.indexOf(candle);
				double[] values = p.getCurrentSelectionValues(candleIndex);
				Color[] colors = p.getCurrentSelectionColors();
				if (values != null && values.length > 0) {
					setProfile(HIGHLIGHT);
					reverseSort(values, colors);

					if (values.length > 2) {
						int middle = values.length / 2;
						textColor = colors[middle];
						refY1 = drawValue(values[middle], p, chart, g, location, candle, -1, true, true);

						textColor = colors[0];
						refY2 = drawValue(values[0], p, chart, g, location, candle, refY1, true, false);

						textColor = colors[values.length - 1];
						refY3 = drawValue(values[values.length - 1], p, chart, g, location, candle, refY1, false, false);
					} else if (values.length == 2) {
						textColor = colors[0];
						refY1 = drawValue(values[0], p, chart, g, location, candle, -1, true, false);
						textColor = colors[1];
						refY2 = drawValue(values[1], p, chart, g, location, candle, refY1, false, false);
					} else {
						textColor = colors[0];
						refY1 = drawValue(values[0], p, chart, g, location, candle, -1, false, true);
					}
					textColor = null;
				}
			}
		}
	}

	private int drawValue(double value, VisualIndicator p, BasicChart<?> chart, Graphics2D g, Point location, Candle candle, int refY, boolean drawAboveRef, boolean inMiddle) {
		Rectangle bounds = p.bounds();
		int y = bounds.y + p.getYCoordinate(Overlay.NONE, value, bounds.height);
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

	private Color getColorForCandle(Candle candle){
		return candle == null ? getBackgroundColor() : candle.isGreen() ? getProfitBackground() : getLossBackground();
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

		if(tagWidth > insets.right){
			tagWidth = insets.right;
		}

		int x = chart.getBoundaryRight() - tagWidth - this.theme().getRightValueTagSpacing();
		if (drawInBox || inMiddle) {
			if (drawInBox) {
				drawStringInBox(x, stringY, chart.getWidth(), tag, g, 1, getColorForCandle(candle));
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
			drawLineToPrice(g, fontHeight, location, x, y, stringY, null);
		}

		return stringY;
	}

	private void drawLineToPrice(Graphics2D g, int fontHeight, Point location, int x, int y, int stringY, Stroke stroke) {
		drawing(g);
		if(stroke != null) {
			g.setStroke(stroke);
		}
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
