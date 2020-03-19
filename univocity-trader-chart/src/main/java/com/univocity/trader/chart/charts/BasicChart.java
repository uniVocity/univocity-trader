package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;
import java.awt.event.*;

public abstract class BasicChart<C extends BasicChartController> extends StaticChart<C> {

	private Point mousePosition = null;
	private int draggingButton = -1;
	private int dragStart;

	public BasicChart(CandleHistoryView candleHistory) {
		super(candleHistory);
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
				if(isMouseDraggingChart()){
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

	public boolean isMouseDragging(){
		return draggingButton != -1 && mousePosition != null && mousePosition.getY() < getHeight() - canvas.getScrollHeight();
	}

	public boolean isMouseDraggingChart(){
		return draggingButton == MouseEvent.BUTTON1 && isMouseDragging();
	}

	public boolean isMouseDraggingCursor(){
		return draggingButton == MouseEvent.BUTTON3 && isMouseDragging();
	}

	public Candle getCandleUnderCursor() {
		if (mousePosition != null) {
			return getCandleAtCoordinate(mousePosition.x);
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

	public final Point getCurrentMousePosition() {
		return mousePosition;
	}

	@Override
	protected void draw(Graphics2D g, int width) {
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
	}

	protected final Stroke getLineStroke() {
		return getController().getNormalStroke();
	}

	protected abstract void drawSelected(Candle selected, Point location, Graphics2D g);

	protected abstract void drawHovered(Candle hovered, Point location, Graphics2D g);
}
