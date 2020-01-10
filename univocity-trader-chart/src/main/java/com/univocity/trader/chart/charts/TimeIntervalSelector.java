package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.gui.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.concurrent.atomic.*;

public class TimeIntervalSelector extends NullLayoutPanel {
	private Handle startHandle = new Handle(true);
	private Handle endHandle = new Handle(false);
	private Handle boundaryHandle = endHandle;

	private static final Color glassBlue = new Color(0, 0, 255, 128);
	private static final Color glassWhite = new Color(255, 255, 255, 128);
	private Point gradientStart = new Point(0, 0);
	private Point gradientEnd = new Point(0, 0);

	private BasicChart<?> chart;
	private BufferedImage background;
	private final EventListenerList listenerList = new EventListenerList();

	private Candle previousStart;
	private Candle previousEnd;

	private boolean dataUpdated;
	private CandleHistory candleHistory;
	private int candlesInRange;

	public TimeIntervalSelector(CandleHistory fullCandleHistory, BasicChart<?> chart) {
		this.candleHistory = fullCandleHistory;
		this.chart = chart;
		this.setPreferredSize(new Dimension(100, 100));

		addMouseMotionListener(new MouseAdapter() {
			private Handle selectedHandle;
			private boolean mouseOverGlass = false;
			private int glassDragStart;

			@Override
			public void mouseDragged(MouseEvent e) {
				if (selectedHandle != null) {
					moveHandle(selectedHandle, e.getPoint().x);
					invokeRepaint();
				} else if (mouseOverGlass) {
					int x = e.getPoint().x;
					int pixelsToMove = x - glassDragStart;

					pixelsToMove = startHandle.getMovablePixels(pixelsToMove);
					if (pixelsToMove != 0) {
						int toMove = endHandle.getMovablePixels(pixelsToMove);
						pixelsToMove = Math.min(toMove, pixelsToMove);
					}

					if (pixelsToMove != 0) {
						glassDragStart = x;
						startHandle.move(pixelsToMove);
						endHandle.move(pixelsToMove);

						updateHandleBoundaries();

						boolean movingRight = pixelsToMove > 0;

						SwingUtilities.invokeLater(() -> {
							int startIndex = chart.getCandleIndexAtCoordinate(startHandle.getPosition());
							int endIndex = startIndex + candlesInRange;

							startHandle.candle = chart.getCandleAtIndex(startIndex);
							endHandle.candle = chart.getCandleAtIndex(endIndex);

							boundaryHandle = movingRight ? endHandle : startHandle;

							repaint();
						});
					}
				} else {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				Point cursor = e.getPoint();
				selectedHandle = startHandle.isCursorOver(cursor) ? startHandle : null;

				if (selectedHandle == null) {
					selectedHandle = endHandle.isCursorOver(cursor) ? endHandle : null;
				}

				if (selectedHandle != null) {
					setCursor(selectedHandle.getCursor());
				} else {
					mouseOverGlass = cursor.x > startHandle.getPosition() && cursor.x < endHandle.getPosition();
					if (mouseOverGlass) {
						glassDragStart = cursor.x;
						setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					} else {
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			}

			private void moveHandle(Handle handle, int x) {
				boundaryHandle = handle;
				updateHandleBoundaries();
				handle.setPosition(x);
				handle.candle = chart.getCandleAtCoordinate(handle.getPosition());
			}
		});

	}

	private final AtomicBoolean intervalUpdateEventRunning = new AtomicBoolean(false);

	void fireIntervalChanged(Candle from, Candle to) {
		if (!intervalUpdateEventRunning.get()) {
			intervalUpdateEventRunning.set(true);
			SwingUtilities.invokeLater(() -> {
				IntervalListener[] listeners = listenerList.getListeners(IntervalListener.class);
				for (IntervalListener listener : listeners) {
					listener.intervalUpdated(from, to);
				}
				intervalUpdateEventRunning.set(false);
			});
		}
	}

	public void addIntervalListener(IntervalListener listener) {
		this.listenerList.add(IntervalListener.class, listener);
	}

	public void removeIntervalListener(IntervalListener listener) {
		this.listenerList.remove(IntervalListener.class, listener);
	}

	private void adjustBoundary() {
		int x = boundaryHandle.getPosition();

		if (x - boundaryHandle.getWidth() <= 0) {
			boundaryHandle.candle = chart.candleHistory.getFirst();
			boundaryHandle.setPosition(0);
		} else if (x + boundaryHandle.getWidth() >= width) {
			boundaryHandle.candle = candleHistory.getLast();
		}

		if (endHandle.candle == candleHistory.getLast()) {
			endHandle.setPosition(getWidth());
		}
	}

	@Override
	public void paintComponent(Graphics g1d) {
		super.paintComponent(g1d);
		Graphics2D g = (Graphics2D) g1d;

		if (dataUpdated || background == null || background.getHeight() != this.getHeight() || background.getWidth() != this.getWidth()) {
			background = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D backgroundGraphics = (Graphics2D) background.getGraphics();
			backgroundGraphics.setColor(Color.WHITE);
			backgroundGraphics.fillRect(0, 0, getWidth(), getHeight());

			chart.setBounds(0, 0, getWidth(), getHeight());
			chart.paintComponent(backgroundGraphics);

			startHandle.draw(g, this, chart);
			endHandle.draw(g, this, chart);
			dataUpdated = false;
			updateHandleBoundaries();
		}
		g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

		adjustBoundary();

		startHandle.draw(g, this, null);
		endHandle.draw(g, this, null);
		drawGlass(g);

		if (startHandle.candle != previousStart || endHandle.candle != previousEnd) {
			previousStart = startHandle.candle;
			previousEnd = endHandle.candle;

			candlesInRange = candleHistory.indexOf(endHandle.candle) - candleHistory.indexOf(startHandle.candle);

			fireIntervalChanged(startHandle.candle, endHandle.candle);
		}
	}

	private void updateHandleBoundaries() {
		int handleWidths = startHandle.getWidth() + endHandle.getWidth();
		endHandle.setMinPosition(startHandle.getPosition() + handleWidths);
		endHandle.setMaxPosition(getWidth());
		startHandle.setMaxPosition(endHandle.getPosition() - handleWidths);
	}

	@Override
	protected void layoutComponents() {

	}

	private void drawGlass(Graphics2D g) {
		int start = startHandle.getPosition() + startHandle.getWidth();
		int end = endHandle.getPosition() - endHandle.getWidth() - start;
		gradientStart.x = getWidth() / 2;
		gradientStart.y = -50;
		gradientEnd.x = getWidth() / 2;
		gradientEnd.y = getHeight() + 50;

		g.setPaint(new GradientPaint(gradientStart, glassBlue, gradientEnd, glassWhite));
		g.fillRect(start, 0, end, getHeight());
	}

	public void dataUpdated() {
		this.dataUpdated = true;
		Candle first = candleHistory.get(0);
		if (startHandle.candle == null) {
			startHandle.candle = first;
		}

		Candle last = candleHistory.get(candleHistory.size() - 1);
		if (endHandle.candle == null) {
			endHandle.candle = last;
		}

		chart.candleHistory.updateView(first, last);

		invokeRepaint();
	}

	private void invokeRepaint() {
		SwingUtilities.invokeLater(this::repaint);
	}
}