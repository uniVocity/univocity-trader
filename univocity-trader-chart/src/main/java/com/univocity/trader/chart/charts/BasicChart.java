package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.theme.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public abstract class BasicChart<T extends PainterTheme<?>> extends StaticChart<T> {

	private final EnumMap<Painter.Overlay, List<Painter<?>>> painters = new EnumMap<>(Painter.Overlay.class);
	private Point mousePosition = null;
	private int draggingButton = -1;
	private int dragStart;
	private int reservedHeight = -1;

	public BasicChart(CandleHistoryView candleHistory) {
		super(candleHistory);

		painters.put(Painter.Overlay.BACK, new ArrayList<>());
		painters.put(Painter.Overlay.FRONT, new ArrayList<>());
		painters.put(Painter.Overlay.NONE, new ArrayList<>());

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
		return getTheme().isVerticalSelectionLineEnabled();
	}

	private boolean isHorizontalSelectionLineEnabled() {
		return getTheme().isHorizontalSelectionLineEnabled();
	}

	private Color getSelectionLineColor() {
		return getTheme().getSelectionLineColor();
	}

	public final Point getCurrentMousePosition() {
		return mousePosition;
	}

	@Override
	protected void draw(Graphics2D g, int width) {
		runPainters(g, Painter.Overlay.NONE, width);

		runPainters(g, Painter.Overlay.BACK, width);

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

	protected final Stroke getLineStroke() {
		return getTheme().getNormalStroke();
	}

	protected abstract void drawSelected(Candle selected, Point location, Graphics2D g);

	protected abstract void drawHovered(Candle hovered, Point location, Graphics2D g);

	private void runPainters(Graphics2D g, Painter.Overlay z, int width) {

		for (Painter<?> painter : painters.get(z)) {
			painter.paintOn(this, g, width);
			canvas.insets.right = Math.max(painter.insets().right, canvas.insets.right);
			canvas.insets.left = Math.max(painter.insets().left, canvas.insets.left);
		}
	}

	public void addPainter(Painter.Overlay overlay, Painter<?> painter) {
		if (painter != null) {
			painters.get(overlay).add(painter);
			painter.install(this);

			if (overlay == Painter.Overlay.NONE) {
				reservedHeight = -1;
			}

			invokeRepaint();
		}
	}

	public void removePainter(Painter<?> painter) {
		if (painter != null) {
			painter.uninstall(this);
			painters.get(Painter.Overlay.FRONT).remove(painter);
			painters.get(Painter.Overlay.BACK).remove(painter);
			painters.get(Painter.Overlay.NONE).remove(painter);
			reservedHeight = -1;
			invokeRepaint();
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
		List<Painter<?>> p;
		p = painters.get(Painter.Overlay.FRONT);
		double minimum = super.getMinimum(from, to);
		for (int i = 0; i < p.size(); i++) {
			minimum = Math.min(p.get(i).minimumValue(from, to), minimum);
		}

		p = painters.get(Painter.Overlay.BACK);
		for (int i = 0; i < p.size(); i++) {
			minimum = Math.min(p.get(i).minimumValue(from, to), minimum);
		}

		return minimum;
	}

	protected int getReservedHeight() {
		if (reservedHeight < 0) {
			reservedHeight = 0;
			List<Painter<?>> all = painters.get(Painter.Overlay.NONE);
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
				bounds.x = available - reservedHeight;
				bounds.height = bounds.height - diff;
				reservedHeight += bounds.height;
			}
		}
		return reservedHeight;
	}
}
