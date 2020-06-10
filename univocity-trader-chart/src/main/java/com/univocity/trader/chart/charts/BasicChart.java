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

	private final EnumMap<Painter.Z, List<Painter<?>>> painters = new EnumMap<>(Painter.Z.class);
	private Point mousePosition = null;
	private int draggingButton = -1;
	private int dragStart;

	public BasicChart(CandleHistoryView candleHistory) {
		super(candleHistory);

		painters.put(Painter.Z.BACK, new ArrayList<>());
		painters.put(Painter.Z.FRONT, new ArrayList<>());

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
		runPainters(g, Painter.Z.BACK, width);

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

		runPainters(g, Painter.Z.FRONT, width);
	}

	protected final Stroke getLineStroke() {
		return getTheme().getNormalStroke();
	}

	protected abstract void drawSelected(Candle selected, Point location, Graphics2D g);

	protected abstract void drawHovered(Candle hovered, Point location, Graphics2D g);

	private void runPainters(Graphics2D g, Painter.Z z, int width) {
		for (Painter<?> painter : painters.get(z)) {
			painter.paintOn(this, g, width);
			canvas.insets.right = Math.max(painter.insets().right, canvas.insets.right);
			canvas.insets.left = Math.max(painter.insets().left, canvas.insets.left);
		}
	}

	public void addPainter(Painter.Z z, Painter<?> painter) {
		if (painter != null) {
			painters.get(z).add(painter);
			painter.install(this);
			invokeRepaint();
		}
	}

	public void removePainter(Painter<?> painter) {
		if (painter != null) {
			painter.uninstall(this);
			painters.get(Painter.Z.BACK).remove(painter);
			painters.get(Painter.Z.FRONT).remove(painter);
			invokeRepaint();
		}
	}

	@Override
	public double getMaximum(int from, int to) {
		List<Painter<?>> p;
		p = painters.get(Painter.Z.FRONT);
		double maximum = super.getMaximum(from, to);
		for (int i = 0; i < p.size(); i++) {
			maximum = Math.max(p.get(i).getMaximumValue(from, to), maximum);
		}

		p = painters.get(Painter.Z.BACK);
		for (int i = 0; i < p.size(); i++) {
			maximum = Math.max(p.get(i).getMaximumValue(from, to), maximum);
		}

		return maximum;
	}

	@Override
	public double getMinimum(int from, int to) {
		List<Painter<?>> p;
		p = painters.get(Painter.Z.FRONT);
		double minimum = super.getMinimum(from, to);
		for (int i = 0; i < p.size(); i++) {
			minimum = Math.min(p.get(i).getMinimumValue(from, to), minimum);
		}

		p = painters.get(Painter.Z.BACK);
		for (int i = 0; i < p.size(); i++) {
			minimum = Math.min(p.get(i).getMinimumValue(from, to), minimum);
		}

		return minimum;
	}
}
