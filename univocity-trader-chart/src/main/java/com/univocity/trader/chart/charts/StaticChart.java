package com.univocity.trader.chart.charts;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.scrolling.*;
import com.univocity.trader.chart.gui.*;

import java.awt.*;
import java.awt.image.*;
import java.util.List;
import java.util.*;

public abstract class StaticChart<C extends BasicChartController> extends NullLayoutPanel {

	private final Insets insets = new Insets(0, 0, 0, 0);

	private final EnumMap<Painter.Z, List<Painter>> painters = new EnumMap<>(Painter.Z.class);

	private double horizontalIncrement = 0.0;
	private double maximum = -1.0;
	private double minimum = Double.MAX_VALUE;

	private double logLow;
	private double logRange;

	private Candle selectedCandle;
	private Candle currentCandle;

	private C controller;

	public final CandleHistoryView candleHistory;

	protected ScrollBar scrollBar;

	private BufferedImage image;
	private long lastPaint;
	private boolean firstRun = true; //repaint on first run to use correct font sizes (first run computes them, second uses them to lay out things correctly).

	public StaticChart(CandleHistoryView candleHistory) {
		this.candleHistory = candleHistory;
		painters.put(Painter.Z.BACK, new ArrayList<>());
		painters.put(Painter.Z.FRONT, new ArrayList<>());
		candleHistory.addDataUpdateListener(this::dataUpdated);
	}

	public void enableScrolling() {
		scrollBar = new ScrollBar(this);
	}

	protected Color getBackgroundColor() {
		return getController().getBackgroundColor();
	}

	protected boolean isAntialiased() {
		return getController().isAntialiased();
	}

	protected void clearGraphics(Graphics g, int width) {
		g.setColor(getBackgroundColor());
		g.fillRect(0, 0, width, height);
	}

	private void applyAntiAliasing(Graphics2D g) {
		if (isAntialiased()) {
			(g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
	}

	public final boolean isDraggingScroll(){
		return isScrollingView() && scrollBar.isDraggingScroll();
	}

	protected final boolean isScrollingView(){
		if(scrollBar == null){
			return false;
		}
		return scrollBar.isScrollingView();
	}

	private void paintImage() {
		if (System.currentTimeMillis() - lastPaint <= 10 && !isScrollingView() && image != null) {
			return;
		}
		final int width = Math.max(getRequiredWidth(), getWidth());

		if (!(image != null && image.getWidth() == width && image.getHeight() == height)) {
			image = new BufferedImage(width, Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
		}

		Graphics2D ig = (Graphics2D) image.getGraphics();

		applyAntiAliasing(ig);
		clearGraphics(ig, width);

		updateScroll();

		insets.right = 0;
		insets.left = 0;

		runPainters(ig, Painter.Z.BACK, width);
		draw(ig, width);
		runPainters(ig, Painter.Z.FRONT, width);

		if(firstRun){
			firstRun = false;
			invokeRepaint();
		}
	}

	public final void paintComponent(Graphics g1d) {
		super.paintComponent(g1d);

		Graphics2D g = (Graphics2D) g1d;

		applyAntiAliasing(g);

		clearGraphics(g, getWidth());

		paintImage();

		g.drawImage(image, 0, 0, getWidth(), height, getBoundaryLeft(), 0, getBoundaryRight(), height, null);

		if (scrollBar != null) {
			scrollBar.draw(g);
		}

		lastPaint = System.currentTimeMillis();
	}

	public int getScrollHeight() {
		return scrollBar != null ? scrollBar.getHeight() : 0;
	}

	public boolean isOverDisabledSectionAtRight(int width, int x){
		return x >= width - (insets.right + getBarWidth() * 1.5);
	}

	public boolean inDisabledSection(Point point) {
		return point.y < getHeight() - scrollBar.getHeight() && (isOverDisabledSectionAtRight(getWidth(), point.x) || point.x < insets.left + getBarWidth());
	}

	public int getInsetsWidth() {
		return insets.left + insets.right;
	}

	private void runPainters(Graphics2D g, Painter.Z z, int width) {
		for (Painter<?> painter : painters.get(z)) {
			painter.paintOn(g, width);
			insets.right = Math.max(painter.insets().right, insets.right);
			insets.left = Math.max(painter.insets().left, insets.left);
		}
	}

	private void updateScroll() {
		if (scrollBar != null) {
			scrollBar.updateScroll();
		}
	}

	public int getBoundaryRight() {
		if (scrollBar != null && scrollBar.isScrollingView()) {
			return scrollBar.getBoundaryRight();
		}
		return width;
	}

	public int getBoundaryLeft() {
		if (scrollBar != null && scrollBar.isScrollingView()) {
			return scrollBar.getBoundaryLeft();
		}
		return 0;
	}


	private void dataUpdated() {
		maximum = 0;
		minimum = Double.MAX_VALUE;

		updateEdgeValues();

		revalidate();
		invokeRepaint();
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
			horizontalIncrement = (((double) Math.max(width, getRequiredWidth()) - (getInsetsWidth())) / (double) candleHistory.size());
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
			invokeRepaint();
		}
	}

	public final void setCurrentCandle(Candle candle) {
		if (this.currentCandle != candle) {
			currentCandle = candle;
			invokeRepaint();
		}
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

	protected final int getBarWidth() {
		return getController().getBarWidth();
	}

	private int getSpaceBetweenCandles() {
		return getController().getSpaceBetweenBars();
	}

	public int getRequiredWidth() {
		return (getBarWidth() + getSpaceBetweenCandles()) * candleHistory.size() + getInsetsWidth();
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

	public int translateX(int x) {
		if (scrollBar != null && scrollBar.isScrollingView()) {
			return x + scrollBar.getBoundaryLeft();
		}
		return x;

	}

	protected abstract void draw(Graphics2D g, int width);

}
