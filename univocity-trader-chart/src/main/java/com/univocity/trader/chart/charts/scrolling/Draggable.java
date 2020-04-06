package com.univocity.trader.chart.charts.scrolling;

import org.apache.commons.lang3.*;

import javax.swing.*;
import java.util.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
abstract class Draggable {
	private int position = Integer.MAX_VALUE;
	private IntConsumer[] positionListeners = new IntConsumer[0];
	private boolean positionUpdateNotificationPending;

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
		boolean moved = this.position != position;
		this.position = position;

		if (moved && positionListeners.length > 0 && !positionUpdateNotificationPending) {
			notifyPositionUpdated();
		}
	}

	private void notifyPositionUpdated() {
		positionUpdateNotificationPending = true;
		SwingUtilities.invokeLater(() -> {
			for (int i = 0; i < positionListeners.length; i++) {
				positionListeners[i].accept(position);
			}
			positionUpdateNotificationPending = false;
		});
	}

	public void addScrollPositionListener(IntConsumer positionListener) {
		if (!ArrayUtils.contains(positionListeners, positionListener)) {
			positionListeners = Arrays.copyOf(positionListeners, positionListeners.length + 1);
			positionListeners[positionListeners.length - 1] = positionListener;
		}
	}

	protected abstract int minPosition();

	protected abstract int maxPosition();
}
