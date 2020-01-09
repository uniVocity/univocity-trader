package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
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

	public TimeIntervalSelector(BasicChart<?> chart) {
		this.chart = chart;
		this.setPreferredSize(new Dimension(100, 100));

		addMouseMotionListener(new MouseMotionListener() {
			private Handle selectedHandle;
			private boolean mouseOverGlass = false;
			private int glassDragStart;

			@Override
			public void mouseDragged(MouseEvent e) {
				if (selectedHandle != null) {
					moveHandle(selectedHandle, e.getPoint().x);
					repaint();
				} else if (mouseOverGlass) {
					int x = e.getPoint().x;
					int pixelsToMove = x - glassDragStart;

					pixelsToMove = startHandle.getMovablePixels(pixelsToMove);
					if (pixelsToMove != 0) {
						int toMove = endHandle.getMovablePixels(pixelsToMove);
						pixelsToMove = Math.min(toMove, pixelsToMove);
					}

					startHandle.move(pixelsToMove);
					endHandle.move(pixelsToMove);

					updateHandleBoundaries();

					startHandle.candle = TimeIntervalSelector.this.chart.getCandleAt(startHandle.getPosition());
					endHandle.candle = TimeIntervalSelector.this.chart.getCandleAt(endHandle.getPosition());

					glassDragStart = x;
					repaint();
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

	private void updateHandleBoundaries() {
		endHandle.setMinPosition(startHandle.getPosition() + startHandle.getWidth());
		endHandle.setMaxPosition(getWidth());
		startHandle.setMaxPosition(endHandle.getPosition() - endHandle.getWidth());
	}

	private void moveHandle(Handle handle, int x) {
		updateHandleBoundaries();
		handle.setPosition(x);
		handle.candle = chart.getCandleAt(handle.getPosition());
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
		}
		g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

		startHandle.draw(g, this, null);
		endHandle.draw(g, this, null);
		drawGlass(g);

		if (startHandle.candle != previousStart || endHandle.candle != previousEnd) {
			previousStart = startHandle.candle;
			previousEnd = endHandle.candle;
			fireIntervalChanged(startHandle.candle, endHandle.candle);
		}
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
		if (startHandle.candle == null) {
			startHandle.candle = chart.tradeHistory.get(0);
		}

		if (endHandle.candle == null) {
			endHandle.candle = chart.tradeHistory.get(chart.tradeHistory.size() - 1);
		}
	}
}

