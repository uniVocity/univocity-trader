package com.univocity.trader.chart;

import com.univocity.trader.candles.*;

import java.awt.*;
import java.util.*;

public abstract class BasicChart extends NullLayoutPanel {

	private boolean displayLogarithmicScale = true;

	private double horizontalIncrement = 0.0;
	private double maximum = -1.0;
	private double minimum = Double.MAX_VALUE;

	private double logLow;
	private double logRange;
	protected Candle selectedCandle;

	private Candle from;
	private Candle to;
	protected final java.util.List<Candle> tradeHistory = new ArrayList<>(1000);

	public BasicChart() {

	}

	protected void clearGraphics(Graphics g) {
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, width, height);
	}

	public final void paintComponent(Graphics g) {
		super.paintComponent(g);

		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		clearGraphics(g);
		draw((Graphics2D) g);
	}

	public void dataUpdated() {
		maximum = 0;
		minimum = Double.MAX_VALUE;

		updateEdgeValues();

		revalidate();
		repaint();
	}

	private void updateEdgeValues() {
		maximum = 0;
		minimum = Integer.MAX_VALUE;
		for (Candle c : tradeHistory) {
			updateEdgeValues(c);
		}
		updateIncrements();
		updateLogarithmicData();
	}

	private void updateLogarithmicData() {
		// update to avoid touching upper and lower limits of the chart
		minimum = minimum * 0.99;
		maximum = maximum * 1.01;

		logLow = Math.log10(minimum);
		logRange = Math.log10(maximum) - logLow;
	}

	private void updateEdgeValues(Candle candle) {
		double value = getHighestPlottedValue(candle);
		if (maximum < value) {
			maximum = value;
		}
		value = getLowestPlottedValue(candle);
		if (minimum > value && value != 0.0) {
			minimum = value;
		}
	}

	private void updateIncrements() {
		if (tradeHistory != null) {
			horizontalIncrement = ((double) width / (double) tradeHistory.size());
		}
	}

	protected final Candle getCandleAt(int x) {
		if (tradeHistory.isEmpty()) {
			return null;
		}
		return tradeHistory.get(getCandleIndexAt(x));
	}

	protected final int getCandleIndexAt(int x) {
		x = (int) ((double) x / horizontalIncrement);
		if (x >= tradeHistory.size()) {
			return tradeHistory.size() - 1;
		}
		return x;
	}

	private int getXCoordinate(int currentPosition) {
		return (int) (currentPosition * horizontalIncrement);
	}

	private int getLogarithmicYCoordinate(double value) {
		return height - (int) ((Math.log10(value) - logLow) * height / logRange);
	}

	private int getLinearYCoordinate(double value) {
		return height - (int) (height * value / maximum);
	}

	private double getValueAtY(int y) {
		if (displayLogarithmicScale) {
			return Math.pow(10, (y + logLow * height / logRange) / height * logRange);
		} else {
			return maximum * y / (height - getLinearYCoordinate(maximum));
		}
	}

	private int getYCoordinate(double value) {
		return displayLogarithmicScale ? getLogarithmicYCoordinate(value) : getLinearYCoordinate(value);
	}

	protected final void layoutComponents() {
		updateIncrements();
	}

	public final Candle getSelectedCandle() {
		return selectedCandle;
	}

	public final void setSelectedCandle(Candle candle) {
		if (this.selectedCandle != candle) {
			selectedCandle = candle;
			repaint();
		}
	}

	protected Point createCandleCoordinate(int candleIndex) {
		Candle candle = tradeHistory.get(candleIndex);

		Point p = new Point();
		p.x = getXCoordinate(candleIndex);
		p.y = getYCoordinate(getCentralValue(candle));

		return p;
	}

	private int indexOf(Candle candle) {
		return Collections.binarySearch(tradeHistory, candle);
	}

	public Point locationOf(Candle candle) {
		if (candle == null) {
			return null;
		}

		Point p = new Point();
		p.x = getX(candle);
		p.y = getY(candle);

		return p;
	}

	public int getX(Candle candle) {
		return getXCoordinate(indexOf(candle));
	}

	public int getY(Candle candle) {
		return getYCoordinate(getCentralValue(candle));
	}

	public Point getSelectedCandleLocation() {
		return locationOf(getSelectedCandle());
	}

	public final int requiredWidth() {
		return (getCandleWidth() + getSpaceBetweenCandles()) * tradeHistory.size();
	}

	protected abstract void draw(Graphics2D g);

	protected abstract int getCandleWidth();

	protected abstract int getSpaceBetweenCandles();

	protected abstract double getHighestPlottedValue(Candle candle);

	protected abstract double getLowestPlottedValue(Candle candle);

	protected abstract double getCentralValue(Candle candle);
}
