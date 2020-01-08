package com.univocity.trader.chart;


import com.univocity.trader.candles.*;

import java.awt.*;
import java.awt.event.*;

public abstract class InteractiveChart extends BasicChart {

	private Candle hoveredCandle = null;
	private Point mousePosition = null;
	private boolean isVerticalSelectionLineEnabled = true;
	private boolean isHorizontalSelectionLineEnabled = true;

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

	@Override
	protected void draw(Graphics2D g) {
		Point hoveredPosition = null;
		if (hoveredCandle != null) {
			hoveredPosition = locationOf(hoveredCandle);
			drawHovered(hoveredCandle, hoveredPosition, g);
		}

		if (isVerticalSelectionLineEnabled || isHorizontalSelectionLineEnabled) {
			if (hoveredCandle != null) {
				g.setColor(new Color(220, 220, 255));
				if (isVerticalSelectionLineEnabled) {
					g.drawLine(hoveredPosition.x, 0, hoveredPosition.x, height);
				}
				if (isHorizontalSelectionLineEnabled) {
					g.drawLine(0, hoveredPosition.y, width, hoveredPosition.y);
				}
			}
		}

		Point selectionPoint = getSelectedCandleLocation();
		if (selectionPoint != null) {
			drawSelected(selectedCandle, selectionPoint, g);
		}
	}

	protected abstract void drawSelected(Candle selected, Point location, Graphics2D g);

	protected abstract void drawHovered(Candle hovered, Point location, Graphics2D g);
}
