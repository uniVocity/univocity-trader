package com.univocity.trader.chart.charts;

import com.univocity.trader.candles.*;

import java.awt.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
class Handle {
	private final boolean leftHandle;
	private int width = 8;
	private int position = 0;
	private int maxPosition = width;
	private int minPosition = 0;
	private Cursor cursor;
	Candle candle;

	private static final Color glassBlack = new Color(0, 0, 0, 128);
	private static final Color glassGray = new Color(128, 128, 128, 128);
	private Point gradientStart = new Point(0, 0);
	private Point gradientEnd = new Point(0, 0);

	public Handle(boolean leftHandle) {
		this.leftHandle = leftHandle;

		if (leftHandle) {
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			setMinPosition(0);
			setPosition(0);
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		}

	}

	public Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	public boolean isCursorOver(Point p) {
		if(leftHandle){
			return p.x >= position && p.x <= position + width;
		} else {
			return p.x >= position - width && p.x <= position;
		}
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public int getMaxPosition() {
		return maxPosition;
	}

	public void setMaxPosition(int maxPosition) {
		this.maxPosition = maxPosition;
		setPosition(position);
	}

	public int getMinPosition() {
		return minPosition;
	}

	public void setMinPosition(int minPosition) {
		this.minPosition = minPosition;
		setPosition(position);
	}

	public void move(int pixels) {
		setPosition(position + pixels);
	}

	public int getMovablePixels(int pixels) {
		int originalPos = position;
		int newPos = position + pixels;
		setPosition(newPos);
		int movable = newPos - position;
		setPosition(originalPos);

		movable = pixels - movable;
		return movable;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		if (position < minPosition) {
			position = minPosition;
		} else if (position > maxPosition) {
			position = maxPosition;
		}
		this.position = position;
	}

	public void draw(Graphics2D g, Component c, BasicChart<?> chart) {
		if (chart != null) {
			Point location = chart.locationOf(candle);
			if (location != null) {
				this.position = location.x;
			}
		}

		int position = this.position;
		if(!leftHandle){
			position = position - width;
		}

		gradientStart.x = position - 2;
		gradientEnd.x = position + (width / 2);
		g.setPaint(new GradientPaint(gradientStart, glassBlack, gradientEnd, glassGray));
		g.fillRect(position, 0, width / 2, c.getHeight());

		gradientStart.x = position + (width / 2);
		gradientEnd.x = position + width + 2;
		g.setPaint(new GradientPaint(gradientStart, glassGray, gradientEnd, glassBlack));
		g.fillRect(position + width / 2, 0, width / 2, c.getHeight());
	}

	public String toString() {
		return position + ", [" + minPosition + " to " + maxPosition + "]";
	}
}
