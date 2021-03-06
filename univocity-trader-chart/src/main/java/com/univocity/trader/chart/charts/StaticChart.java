package com.univocity.trader.chart.charts;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.awt.image.*;

public abstract class StaticChart<T extends PainterTheme<?>> extends CoordinateManager implements Repaintable {

	private double horizontalIncrement = 0.0;

	private Candle selectedCandle;
	private Candle currentCandle;

	private Candle firstVisibleCandle;
	private Candle lastVisibleCandle;

	private T theme;

	public final CandleHistoryView candleHistory;

	private BufferedImage image;
	private long lastPaint;
	public final ChartCanvas canvas;

	public StaticChart(CandleHistoryView candleHistory) {
		this(new ChartCanvas(), candleHistory);
	}

	public StaticChart(ChartCanvas canvas, CandleHistoryView candleHistory) {
		this.canvas = canvas;
		this.canvas.setChart(this);
		this.candleHistory = candleHistory;
		candleHistory.addDataUpdateListener(this::dataUpdated);
		canvas.addScrollPositionListener(this::onScrollPositionUpdate);
	}

	protected Color getBackgroundColor() {
		return theme().getBackgroundColor();
	}

	protected boolean isAntialiased() {
		return theme().isAntialiased();
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

		draw(ig, width);
	}

	public final void paintComponent(Graphics2D g) {
		applyAntiAliasing(g);

		clearGraphics(g, getWidth());

		paintImage();

		int imgTo = getBoundaryRight();
		int imgFrom = imgTo - getWidth();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), imgFrom, 0, imgTo, getHeight(), null);

		paintOver(g);

		lastPaint = System.currentTimeMillis();
	}

	protected void paintOver(Graphics2D g) {

	}

	public int getBoundaryLeft() {
		return canvas.getBoundaryLeft();
	}

	public int getBoundaryRight() {
		return canvas.getBoundaryRight();
	}

	void onScrollPositionUpdate(int newPosition) {
		onScrollPositionUpdate();

		int from = firstVisibleCandle == null ? 0 : candleHistory.indexOf(firstVisibleCandle);
		int to = lastVisibleCandle == null ? candleHistory.size() : candleHistory.indexOf(lastVisibleCandle);

		updateEdgeValues(theme.isDisplayingLogarithmicScale(), from, to);
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

	double getMaximum(int from, int to) {
		return getMaximum();
	}

	double getMinimum(int from, int to) {
		return getMinimum();
	}

	@Override
	protected final void updateMinAndMax(boolean logScale, int from, int to) {
		onScrollPositionUpdate();
		for (int i = from; i < to; i++) {
			Candle candle = candleHistory.get(i);
			if (candle == null) {
				continue;
			}

			double value = getHighestPlottedValue(candle);
			if (maximum < value) {
				maximum = value;
			}
			value = getLowestPlottedValue(candle);
			if (minimum > value) {
				minimum = value;
			}
		}

		if (firstVisibleCandle != null && lastVisibleCandle != null) {
			maximum = Math.max(maximum, getMaximum(from, to));
			minimum = Math.min(minimum, getMinimum(from, to));
		}
		updateIncrements();
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

	public int getXCoordinate(int currentPosition) {
		return (int) Math.round(currentPosition * horizontalIncrement);
	}

	private int getLogarithmicYCoordinate(double value) {
		return getLogarithmicYCoordinate(value, getAvailableHeight());
	}

	private int getLinearYCoordinate(double value) {
		return getLinearYCoordinate(value, getAvailableHeight());
	}

	public double getValueAtY(int y) {
		return getValueAtY(y, getAvailableHeight());
	}

	public final int getYCoordinate(double value) {
		return displayLogarithmicScale() ? getLogarithmicYCoordinate(value) : getLinearYCoordinate(value);
	}

	private boolean displayLogarithmicScale() {
		return theme().isDisplayingLogarithmicScale();
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

	@Override
	public final void invokeRepaint() {
		canvas.invokeRepaint();
	}

	public final int getWidth() {
		return canvas.getWidth();
	}

	public final Candle getCurrentCandle() {
		return currentCandle;
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

	protected Point createCandleCoordinate(int candleIndex, Candle candle, int imgFrom, int imgTo) {
		int x = getXCoordinate(candleIndex);
		if (x >= imgFrom && x <= imgTo) {
			return new Point(x, getYCoordinate(getCentralValue(candle)));
		}
		return null;
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
		return theme().getBarWidth();
	}

	public int getSpaceBetweenCandles() {
		return theme().getSpaceBetweenBars();
	}

	public int calculateRequiredWidth() {
		return (canvas.getBarWidth() + getSpaceBetweenCandles()) * candleHistory.size() + canvas.getInsetsWidth();
	}

	protected abstract T newTheme();

	@Override
	public final T theme() {
		if (theme == null) {
			this.theme = newTheme();
		}
		return theme;
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
		return canvas.getHeight();
	}

	public int getAvailableHeight() {
		return getHeight() - getReservedHeight();
	}

	protected int getReservedHeight() {
		return 0;
	}

	public int getBarWidth() {
		return canvas.getBarWidth();
	}
}
