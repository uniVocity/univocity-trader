package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;
import java.awt.event.*;

public abstract class InteractiveChart<C extends InteractiveChartController> extends BasicChart<C> {

	private Candle hoveredCandle = null;
	private Point mousePosition = null;

	public InteractiveChart() {
		this.setFocusable(true);

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				processMouseEvent(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				processMouseEvent(e);
			}

			private void processMouseEvent(final MouseEvent e) {
				mousePosition = e.getPoint();
				Candle candle = getCandleUnderCursor();
				if (candle != hoveredCandle) {
					System.out.println(candle);
					hoveredCandle = candle;
				}
				repaint();
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				mousePosition = null;
			}
		});
	}

	public Candle getCandleUnderCursor() {
		if (mousePosition != null) {
			return getCandleAt(mousePosition.x);
		}
		return null;
	}

	private boolean isVerticalSelectionLineEnabled() {
		return getController().isVerticalSelectionLineEnabled();
	}

	private boolean isHorizontalSelectionLineEnabled() {
		return getController().isHorizontalSelectionLineEnabled();
	}

	private Color getSelectionLineColor() {
		return getController().getSelectionLineColor();
	}

	@Override
	protected void draw(Graphics2D g) {
		Point hoveredPosition = locationOf(hoveredCandle);

		if (isVerticalSelectionLineEnabled() || isHorizontalSelectionLineEnabled()) {
			g.setStroke(new BasicStroke(1));
			if (hoveredPosition != null) {
				g.setColor(getSelectionLineColor());
				if (isVerticalSelectionLineEnabled()) {
					g.drawLine(hoveredPosition.x, 0, hoveredPosition.x, height);
				}
				if (isHorizontalSelectionLineEnabled()) {
					g.drawLine(0, hoveredPosition.y, width, hoveredPosition.y);
				}
			}
		}

		Point selectionPoint = getSelectedCandleLocation();
		if (selectionPoint != null) {
			drawSelected(selectedCandle, selectionPoint, g);
		}
		if (hoveredPosition != null) {
			drawHovered(hoveredCandle, hoveredPosition, g);
		}
	}

	protected final Stroke getLineStroke() {
		return getController().getNormalStroke();
	}

	protected abstract void drawSelected(Candle selected, Point location, Graphics2D g);

	protected abstract void drawHovered(Candle hovered, Point location, Graphics2D g);
}
