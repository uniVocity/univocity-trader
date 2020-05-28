package com.univocity.trader.chart.charts;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.painter.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.util.List;
import java.util.*;

public abstract class StaticChart<C extends BasicChartController> {

	private final EnumMap<Painter.Z, List<Painter<?>>> painters = new EnumMap<>(Painter.Z.class);

	private double horizontalIncrement = 0.0;
	private double maximum = -1.0;
	private double minimum = Double.MAX_VALUE;

	private double logLow;
	private double logRange;

	private Candle selectedCandle;
	private Candle currentCandle;

	private Candle firstVisibleCandle;
	private Candle lastVisibleCandle;

	private C controller;

	public final CandleHistoryView candleHistory;

	private BufferedImage image;
	private long lastPaint;
	private boolean firstRun = true; //repaint on first run to use correct font sizes (first run computes them, second uses them to lay out things correctly).

	public final ChartCanvas canvas;

	private int height = -1;

	public StaticChart(CandleHistoryView candleHistory) {
		this(new ChartCanvas(), candleHistory);
	}

	public StaticChart(ChartCanvas canvas, CandleHistoryView candleHistory) {
		this.canvas = canvas;
		this.canvas.addChart(this);
		this.candleHistory = candleHistory;
		painters.put(Painter.Z.BACK, new ArrayList<>());
		painters.put(Painter.Z.FRONT, new ArrayList<>());
		candleHistory.addDataUpdateListener(this::dataUpdated);
		canvas.addScrollPositionListener(this::onScrollPositionUpdate);
	}

	protected Color getBackgroundColor() {
		return getController().getBackgroundColor();
	}

	protected boolean isAntialiased() {
		return getController().isAntialiased();
	}

	protected void clearGraphics(Graphics g, int width) {
		g.setColor(getBackgroundColor());
		g.fillRect(0, 0, width, getHeight());
	}

	private void applyAntiAliasing(Graphics2D g) {
		if (isAntialiased()) {
			(g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
	}


	private void paintImage() {
		if (System.currentTimeMillis() - lastPaint <= 10 && !canvas.isScrollingView() && image != null) {
			return;
		}
		final int width = Math.max(getRequiredWidth(), getWidth());

		if (!(image != null && image.getWidth() == width && image.getHeight() == getHeight())) {
			image = new BufferedImage(Math.max(1, width), Math.max(1, getHeight()), BufferedImage.TYPE_INT_ARGB);
		}

		Graphics2D ig = (Graphics2D) image.getGraphics();

		applyAntiAliasing(ig);
		clearGraphics(ig, width);

		runPainters(ig, Painter.Z.BACK, width);
		draw(ig, width);
		runPainters(ig, Painter.Z.FRONT, width);

		if (firstRun) {
			firstRun = false;
			invokeRepaint();
		}
	}

	public final void paintComponent(Graphics2D g) {
		applyAntiAliasing(g);

		clearGraphics(g, getWidth());

		paintImage();

		g.drawImage(image, 0, 0, getWidth(), getHeight(), getBoundaryLeft(), 0, getBoundaryRight(), getHeight(), null);

		lastPaint = System.currentTimeMillis();
	}

	public int getBoundaryLeft() {
		return canvas.getBoundaryLeft();
	}

	public int getBoundaryRight() {
		return canvas.getBoundaryRight();
	}

	private void runPainters(Graphics2D g, Painter.Z z, int width) {
		for (Painter<?> painter : painters.get(z)) {
			painter.paintOn(g, width);
			canvas.insets.right = Math.max(painter.insets().right, canvas.insets.right);
			canvas.insets.left = Math.max(painter.insets().left, canvas.insets.left);
		}
	}

	private void onScrollPositionUpdate(int newPosition) {
		onScrollPositionUpdate();
		updateEdgeValues();
		invokeRepaint();
	}

	private void onScrollPositionUpdate() {
		if (canvas.isScrollingView()) {
			updateIncrements();
			this.firstVisibleCandle = getCandleAtCoordinate(canvas.scrollBar.getBoundaryLeft());
			this.lastVisibleCandle = getCandleAtCoordinate(canvas.scrollBar.getBoundaryRight());
		} else {
			this.firstVisibleCandle = null;
			this.lastVisibleCandle = null;
		}
	}

	private void dataUpdated(CandleHistory.UpdateType type) {
		canvas.updateScroll();
		onScrollPositionUpdate(-1);
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
		minimum = minimum * 0.995;
		maximum = maximum * 1.0005;

		updateLogarithmicData();
	}

	private void updateLogarithmicData() {
		logLow = Math.log10(minimum);
		logRange = Math.log10(maximum) - logLow;
	}

	private void updateEdgeValues(Candle candle) {
		if ((firstVisibleCandle != null && candle.openTime < firstVisibleCandle.openTime) || (lastVisibleCandle != null && candle.closeTime > lastVisibleCandle.closeTime)) {
			return;
		}

		double value = getHighestPlottedValue(candle);
		if (maximum < value) {
			maximum = value;
		}
		value = getLowestPlottedValue(candle);
		if (minimum > value && value != 0.0) {
			minimum = value;
		}
	}

	public int getRequiredWidth() {
		return canvas.getRequiredWidth();
	}

	private void updateIncrements() {
		if (!candleHistory.isEmpty()) {
			horizontalIncrement = (((double) Math.max(getWidth(), getRequiredWidth()) - (canvas.getInsetsWidth())) / (double) candleHistory.size());
		}
	}

	public double getHorizontalIncrement() {
		return horizontalIncrement;
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
		return getHeight() - (int) ((Math.log10(value) - logLow) * getHeight() / logRange);
	}

	private int getLinearYCoordinate(double value) {
		double linearRange = getHeight() - (getHeight() * minimum / maximum);
		double proportion = (getHeight() - (getHeight() * value / maximum)) / linearRange;

		return (int) (getHeight() * proportion);
	}

	public double getValueAtY(int y) {
		if (displayLogarithmicScale()) {
			return Math.pow(10, (y + logLow * getHeight() / logRange) / getHeight() * logRange);
		} else {
			return minimum + ((maximum - minimum) / getHeight()) * y;
		}
	}

	public final int getYCoordinate(double value) {
		return displayLogarithmicScale() ? getLogarithmicYCoordinate(value) : getLinearYCoordinate(value);
	}

	private boolean displayLogarithmicScale() {
		return getController().isDisplayingLogarithmicScale();
	}

	public final void layoutComponents() {
		updateIncrements();
	}

	public final Candle getSelectedCandle() {
		return selectedCandle;
	}

	public final void setSelectedCandle(Candle candle) {
		if (this.selectedCandle != candle) {
			selectedCandle = candle;
			invokeRepaint();
		}
	}

	public final void setCurrentCandle(Candle candle) {
		if (this.currentCandle != candle) {
			currentCandle = candle;
			invokeRepaint();
		}
	}

	public final void invokeRepaint() {
		canvas.invokeRepaint();
	}

	public final int getWidth() {
		return canvas.getWidth();
	}

	public final Candle getCurrentCandle() {
		return currentCandle;
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

	public Point getCurrentCandleLocation() {
		return locationOf(getCurrentCandle());
	}

	public final int calculateBarWidth() {
		return getController().getBarWidth();
	}

	private int getSpaceBetweenCandles() {
		return getController().getSpaceBetweenBars();
	}

	public int calculateRequiredWidth() {
		return (canvas.getBarWidth() + getSpaceBetweenCandles()) * candleHistory.size() + canvas.getInsetsWidth();
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

	public double getHighestPlottedValue(Candle candle) {
		return candle.high;
	}

	public double getLowestPlottedValue(Candle candle) {
		return candle.low;
	}

	public double getCentralValue(Candle candle) {
		return candle.close;
	}

	protected abstract void draw(Graphics2D g, int width);

	public int getHeight() {
		return height < 0 ? canvas.getHeight() : height;
	}

	public final void setHeight(int height) {
		this.height = height;
	}

	public int getBarWidth() {
		return canvas.getBarWidth();
	}
}
