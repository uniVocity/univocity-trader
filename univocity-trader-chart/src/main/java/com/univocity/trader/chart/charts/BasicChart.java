package com.univocity.trader.chart.charts;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.scrolling.*;
import com.univocity.trader.chart.gui.*;

import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class BasicChart<C extends BasicChartController> extends NullLayoutPanel {

	private final EnumMap<Painter.Z, List<Painter>> painters = new EnumMap<>(Painter.Z.class);


	private double horizontalIncrement = 0.0;
	private double maximum = -1.0;
	private double minimum = Double.MAX_VALUE;

	private double logLow;
	private double logRange;

	protected Candle selectedCandle;

	private C controller;

	public final CandleHistoryView candleHistory;

	private ScrollBar scrollBar;

	public BasicChart(CandleHistoryView candleHistory) {
		this.candleHistory = candleHistory;
		painters.put(Painter.Z.BACK, new ArrayList<>());
		painters.put(Painter.Z.FRONT, new ArrayList<>());
		candleHistory.addDataUpdateListener(this::dataUpdated);
	}

	public void enableScrolling(){
		scrollBar = new ScrollBar(this);
	}

	protected Color getBackgroundColor() {
		return getController().getBackgroundColor();
	}

	protected boolean isAntialiased() {
		return getController().isAntialiased();
	}

	protected void clearGraphics(Graphics g) {
		g.setColor(getBackgroundColor());
		g.fillRect(0, 0, width, height);
	}

	public final void paintComponent(Graphics g1d) {
		super.paintComponent(g1d);
		Graphics2D g = (Graphics2D) g1d;

		if (isAntialiased()) {
			(g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		clearGraphics(g);

		for (Painter<?> painter : painters.get(Painter.Z.BACK)) {
			painter.paintOn(g);
		}

		draw(g);

		for (Painter<?> painter : painters.get(Painter.Z.FRONT)) {
			painter.paintOn(g);
		}

		if(scrollBar != null) {
			scrollBar.draw(g);
		}
	}

	private void dataUpdated() {
		maximum = 0;
		minimum = Double.MAX_VALUE;

		updateEdgeValues();

		revalidate();
		repaint();
	}

	public double getMaximum() {
		return maximum;
	}

	private void updateEdgeValues() {
		maximum = 0;
		minimum = Integer.MAX_VALUE;

		for (Candle c : candleHistory) {
			updateEdgeValues(c);
		}
		updateIncrements();

		// avoids touching upper and lower limits of the chart
		minimum = minimum * 0.99;
		maximum = maximum * 1.01;

		updateLogarithmicData();
	}

	private void updateLogarithmicData() {
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
		if (!candleHistory.isEmpty()) {
			horizontalIncrement = ((double) width / (double) candleHistory.size());
		}
	}

	public final Candle getCandleAtIndex(int index) {
		return candleHistory.get(index);
	}

	public final Candle getCandleAtCoordinate(int x) {
		return candleHistory.get(getCandleIndexAtCoordinate(x));
	}

	public final int getCandleIndexAtCoordinate(int x) {
		x = (int) Math.round((double) x / horizontalIncrement);
		if (x >= candleHistory.size()) {
			return candleHistory.size() - 1;
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
		double linearRange = height - (height * minimum / maximum);
		double proportion = (height - (height * value / maximum)) / linearRange;

		return (int) (height * proportion);
	}

	public double getValueAtY(int y) {
		if (displayLogarithmicScale()) {
			return Math.pow(10, (y + logLow * height / logRange) / height * logRange);
		} else {
			return maximum * y / (height - getLinearYCoordinate(maximum));
		}
	}

	public final int getYCoordinate(double value) {
		return displayLogarithmicScale() ? getLogarithmicYCoordinate(value) : getLinearYCoordinate(value);
	}

	private boolean displayLogarithmicScale() {
		return getController().isDisplayingLogarithmicScale();
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
		Candle candle = candleHistory.get(candleIndex);

		Point p = new Point();
		p.x = getXCoordinate(candleIndex);
		p.y = getYCoordinate(getCentralValue(candle));

		return p;
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
		return getXCoordinate(candleHistory.indexOf(candle));
	}

	public int getY(Candle candle) {
		return getYCoordinate(getCentralValue(candle));
	}

	public Point getSelectedCandleLocation() {
		return locationOf(getSelectedCandle());
	}

	protected final int getBarWidth() {
		return getController().getBarWidth();
	}

	private int getSpaceBetweenCandles() {
		return getController().getSpaceBetweenBars();
	}

	public int requiredWidth() {
		return (getBarWidth() + getSpaceBetweenCandles()) * candleHistory.size();
	}

	protected abstract C newController();

	public final C getController() {
		if (controller == null) {
			this.controller = newController();
		}
		return controller;
	}

	public void register(Painter<?> painter) {
		painters.get(painter.getZ()).add(painter);
	}

	protected double getHighestPlottedValue(Candle candle) {
		return candle.high;
	}

	protected double getLowestPlottedValue(Candle candle) {
		return candle.low;
	}

	public double getCentralValue(Candle candle) {
		return candle.close;
	}

	protected abstract void draw(Graphics2D g);

}
