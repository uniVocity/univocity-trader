package com.univocity.trader.chart.charts;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
abstract class Draggable {
	private int position;

	public final int getPosition() {
		return position;
	}

	public final int getMovablePixels(int pixels) {
		int originalPos = position;
		int newPos = position + pixels;
		setPosition(newPos);
		int movable = newPos - position;
		setPosition(originalPos);

		movable = pixels - movable;
		return movable;
	}

	public final void move(int pixels) {
		setPosition(position + pixels);
	}

	public final void setPosition(int position) {
		if (position < minPosition()) {
			position = minPosition();
		} else if (position > maxPosition()) {
			position = maxPosition();
		}
		this.position = position;
	}

	protected abstract int minPosition();

	protected abstract int maxPosition();
}
