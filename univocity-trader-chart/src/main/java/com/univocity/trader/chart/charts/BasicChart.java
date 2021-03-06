package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;
import com.univocity.trader.chart.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

import static com.univocity.trader.chart.charts.painter.Painter.Overlay.*;

public abstract class BasicChart<T extends PainterTheme<?>> extends StaticChart<T> {

	private final EnumMap<Painter.Overlay, List<Painter<?>>> painters = new EnumMap<>(Painter.Overlay.class);
	private Point mousePosition = null;
	private int draggingButton = -1;
	private int dragStart;
	private int reservedHeight = -1;

	private Painter<?> hoveredPainter;
	private Painter<?> selectedPainter;
	private List<Consumer<Painter<?>>> painterSelectedListeners = new ArrayList<>();

	public BasicChart(CandleHistoryView candleHistory) {
		super(candleHistory);

		painters.put(Painter.Overlay.BACK, new ArrayList<>());
		painters.put(Painter.Overlay.FRONT, new ArrayList<>());
		painters.put(NONE, new ArrayList<>());

		canvas.setFocusable(true);

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				draggingButton = -1;
				Candle current = getCurrentCandle();
				Candle selected = getSelectedCandle();

				if (current != selected) {
					setSelectedCandle(current);
				} else {
					setSelectedCandle(null);
				}

				if (hoveredPainter != null) {
					setSelectedPainter(hoveredPainter == selectedPainter ? null : hoveredPainter);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				draggingButton = -1;
				invokeRepaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				draggingButton = e.getButton();
				invokeRepaint();
			}
		});

		canvas.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (isMouseDraggingChart()) {
					dragStart = canvas.scrollBar.drag(e, dragStart);
				}
				processMouseEvent(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				processMouseEvent(e);
			}

			private void processMouseEvent(final MouseEvent e) {
				dragStart = e.getX();
				mousePosition = e.getPoint();
				if (canvas.inDisabledSection(mousePosition)) {
					return;
				}
				mousePosition.x = canvas.translateX(mousePosition.x);
				Candle candle = getCandleUnderCursor();
				if (candle != getCurrentCandle()) {
					setCurrentCandle(candle);
				}
				invokeRepaint();
			}
		});

		canvas.addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				mousePosition = null;
			}
		});
	}

	public boolean isMouseDragging() {
		return draggingButton != -1 && mousePosition != null && mousePosition.getY() < getHeight() - canvas.getScrollHeight();
	}

	public boolean isMouseDraggingChart() {
		return draggingButton == MouseEvent.BUTTON1 && isMouseDragging();
	}

	public boolean isMouseDraggingCursor() {
		return draggingButton == MouseEvent.BUTTON3 && isMouseDragging();
	}

	public Candle getCandleUnderCursor() {
		if (mousePosition != null) {
			return getCandleAtCoordinate(mousePosition.x);
		}
		return null;
	}

	private boolean isVerticalSelectionLineEnabled() {
		return theme().isVerticalSelectionLineEnabled();
	}

	private boolean isHorizontalSelectionLineEnabled() {
		return theme().isHorizontalSelectionLineEnabled();
	}

	private Color getSelectionLineColor() {
		return theme().getSelectionLineColor();
	}

	public final Point getCurrentMousePosition() {
		return mousePosition;
	}

	protected void prepareToDraw(Graphics2D g){

	}

	protected abstract void doDraw(Graphics2D g, int i, Candle candle, Point current, Point previous);

	@Override
	protected final void draw(Graphics2D g, int width) {
		final int imgTo = getBoundaryRight();
		final int imgFrom = imgTo - getWidth();

		prepareToDraw(g);
		Point prev = null;
		for (int i = 0; i < candleHistory.size(); i++) {
			Candle candle = candleHistory.get(i);
			if (candle == null) {
				break;
			}
			Point current = createCandleCoordinate(i, candle, imgFrom, imgTo);
			if(current != null){
				doDraw(g, i, candle, current, prev);
				prev = current;
			} else if(prev != null) {
				break;
			}
		}

		runPainters(g, Painter.Overlay.BACK, width);

		for (Painter<?> painter : painters.get(NONE)) {
			int y = painter.bounds().y;
			g.setPaint(new GradientPaint(0, -100, theme().getInverseBackgroundColor(), 0, y + 20, theme().getTransparentBackgroundColor()));
			g.fillRect(0, y, width - canvas.insets.right - 1, 20);
		}

		runPainters(g, NONE, width);

		Point hoveredPosition = getCurrentCandleLocation();

		if (isVerticalSelectionLineEnabled() || isHorizontalSelectionLineEnabled()) {
			g.setStroke(new BasicStroke(1));
			if (hoveredPosition != null) {
				g.setColor(getSelectionLineColor());
				if (isVerticalSelectionLineEnabled()) {
					g.drawLine(hoveredPosition.x, 0, hoveredPosition.x, getHeight());
				}
				if (isHorizontalSelectionLineEnabled()) {
					g.drawLine(0, hoveredPosition.y, width, hoveredPosition.y);
				}
			}
		}

		Point selectionPoint = getSelectedCandleLocation();
		if (selectionPoint != null) {
			drawSelected(getSelectedCandle(), selectionPoint, g);
		}
		if (hoveredPosition != null) {
			drawHovered(getCurrentCandle(), hoveredPosition, g);
		}

		runPainters(g, Painter.Overlay.FRONT, width);
	}

	protected void paintOver(Graphics2D g) {
		int y = 0;
		this.hoveredPainter = null;
		y = printPainterHeaders(Painter.Overlay.BACK, g, y);
		y = printPainterHeaders(Painter.Overlay.FRONT, g, y);
		printPainterHeaders(NONE, g, y);
	}

	private int printPainterHeaders(Painter.Overlay overlay, Graphics2D g, int y) {
		for (Painter<?> painter : painters.get(overlay)) {
			String header = painter.header();
			if (header == null || header.isBlank()) {
				continue;
			}

			if (overlay == NONE) {
				y = painter.bounds().y;
			}

			y += 15;
			Font font = theme().getHoveredHeaderFont();
			Color headerColor = theme().getHeaderColor();
			if (mousePosition != null && mousePosition.y >= y - 20 && mousePosition.y <= y + 5) {
				headerColor = theme().getSelectedHeaderColor();
				hoveredPainter = painter;
			}

			if (painter == selectedPainter) {
				font = theme().getSelectedHeaderFont();
			}

			if (hoveredPainter == painter) {
				GraphicUtils.drawStringInBox(5, y, header, g, 1, theme().getTransparentBackgroundColor(), font, headerColor);
			} else {
				g.setFont(font);
				g.setColor(headerColor);
				g.drawString(header, 5, y);
			}

			y += 5;
		}

		this.canvas.setCursor(Cursor.getPredefinedCursor(hoveredPainter == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));

		return y;
	}

	protected final Stroke getLineStroke() {
		return theme().getNormalStroke();
	}

	protected abstract void drawSelected(Candle selected, Point location, Graphics2D g);

	protected abstract void drawHovered(Candle hovered, Point location, Graphics2D g);

	private void runPainters(Graphics2D g, Painter.Overlay overlay, int width) {
		for (Painter<?> painter : Collections.unmodifiableList(painters.get(overlay))) {
			painter.paintOn(this, g, width, overlay);
			canvas.insets.right = Math.max(painter.insets().right, canvas.insets.right);
			canvas.insets.left = Math.max(painter.insets().left, canvas.insets.left);
		}
	}

	public int painterCount(Painter.Overlay overlay) {
		return painters.get(overlay).size();
	}

	public void addPainter(Painter.Overlay overlay, Painter<?> painter) {
		if (painter != null) {
			var painterList = painters.get(overlay);

			if (painter.position() == -1) {
				painter.position(painterList.size());
				painterList.add(painter);
			} else {
				if (painter.position() > painterList.size()) {
					painterList.add(painter);
					int i = 0;
					for (Painter<?> p : painterList) {
						p.position(i++);
					}
				} else {
					painterList.add(painter.position(), painter);
				}
			}

			painter.install(this);

			if (overlay == NONE) {
				reservedHeight = -1;
			}

			onScrollPositionUpdate(-1);
			invokeRepaint();
		}
	}

	public void removePainter(Painter<?> painter) {
		if (painter != null) {
			painter.uninstall(this);
			painters.get(Painter.Overlay.FRONT).remove(painter);
			painters.get(Painter.Overlay.BACK).remove(painter);
			painters.get(NONE).remove(painter);
			reservedHeight = -1;
			invokeRepaint();
		}
	}

	@Override
	public void updateEdgeValues(boolean logScale, int from, int to) {
		super.updateEdgeValues(logScale, from, to);
		List<Painter<?>> p = painters.get(NONE);
		for (int i = 0; i < p.size(); i++) {
			((AreaPainter) p.get(i)).updateEdgeValues(false, from, to);
		}
	}


	@Override
	public double getMaximum(int from, int to) {
		List<Painter<?>> p;
		p = painters.get(Painter.Overlay.FRONT);
		double maximum = super.getMaximum(from, to);
		for (int i = 0; i < p.size(); i++) {
			maximum = Math.max(p.get(i).maximumValue(from, to), maximum);
		}

		p = painters.get(Painter.Overlay.BACK);
		for (int i = 0; i < p.size(); i++) {
			maximum = Math.max(p.get(i).maximumValue(from, to), maximum);
		}

		return maximum;
	}

	@Override
	public double getMinimum(int from, int to) {
		double minimum = super.getMinimum(from, to);
		boolean logScale = theme().isDisplayingLogarithmicScale();

		minimum = getMinimum(minimum, Painter.Overlay.FRONT, from, to, logScale);
		minimum = getMinimum(minimum, Painter.Overlay.BACK, from, to, logScale);

		return minimum;
	}

	private double getMinimum(double minimum, Painter.Overlay overlay, int from, int to, boolean logScale) {
		List<Painter<?>> p = painters.get(overlay);
		for (int i = 0; i < p.size(); i++) {
			double min = p.get(i).minimumValue(logScale, from, to);
			if (!logScale || min > 0) {
				minimum = Math.min(min, minimum);
			}
		}
		return minimum;
	}

	protected int getReservedHeight() {
		if (reservedHeight < 0) {
			reservedHeight = 0;
			List<Painter<?>> all = new ArrayList<>(painters.get(NONE));
			if (all.size() == 0) {
				return 0;
			}

			final int available = getHeight();
			int maxIndividualHeight = (int) (available * 0.8 / all.size());
			for (Painter<?> painter : all) {
				Rectangle bounds = painter.bounds();
				if (bounds.height == 0) {
					bounds.height = (int) (available * 0.2);
				}

				int diff = bounds.height > maxIndividualHeight ? bounds.height - maxIndividualHeight : 0;
				bounds.height = bounds.height - diff;
				reservedHeight += bounds.height;
				bounds.y = available - reservedHeight;
			}
		}
		return reservedHeight;
	}

	public List<Painter<?>> underlays() {
		return painters.get(NONE);
	}

	public void addPainterSelectedListener(Consumer<Painter<?>> listener) {
		painterSelectedListeners.add(listener);
	}

	private void setSelectedPainter(Painter<?> painter) {
		this.selectedPainter = painter;
		if (painter != null) {
			painterSelectedListeners.forEach(c -> c.accept(painter));
		}
	}
}
