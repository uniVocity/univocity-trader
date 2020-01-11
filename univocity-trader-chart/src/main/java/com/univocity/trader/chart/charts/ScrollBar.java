package com.univocity.trader.chart.charts;

import com.univocity.trader.chart.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
class ScrollBar extends MouseAdapter {

	private static final Color glassBlue = new Color(0, 0, 255, 128);
	private static final Color barGray = new Color(128, 128, 128, 64);
	private final Point gradientStart = new Point(0, 0);
	private final Point gradientEnd = new Point(0, 0);

	final NullLayoutPanel parent;

	int height = 10;
	boolean scrolling;
	private boolean scrollRequired;
	private boolean dragging;
	private int dragStart;

	final ScrollHandle scrollHandle = new ScrollHandle(this);

	ScrollBar(NullLayoutPanel parent) {
		this.parent = parent;
		parent.addMouseMotionListener(this);

		Timer timer = new Timer(500, (e) -> {
			if (!dragging) {
				Point p = MouseInfo.getPointerInfo().getLocation();
				p = new Point(p.x - parent.getLocation().x, p.y - parent.getLocation().y);
				updateHighlight(p);
			}
		}
		);
		timer.start();
	}

	void draw(Graphics2D g) {
		int required = parent.requiredWidth();
		int available = parent.getWidth();
		scrollRequired = required > available;

		gradientStart.x = parent.getWidth() / 2;
		gradientStart.y = -50;
		gradientEnd.x = parent.getWidth() / 2;
		gradientEnd.y = height + 50;

		g.setPaint(new GradientPaint(gradientStart, glassBlue, gradientEnd, barGray));
		g.fillRect(0, parent.getHeight() - height, parent.getWidth(), height);

		if (scrollRequired) {
			int areaOutOfView = available - (int) ((double) available * Math.round((double) required / (double) available - 1.0));

//			scrollHandle.setWidth(handleWidth);
			scrollHandle.draw(g, parent);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (scrollRequired) {
			if (scrolling) {
				dragging = true;

				int pixelsToMove = e.getX() - dragStart;

				pixelsToMove = scrollHandle.getMovablePixels(pixelsToMove);
				System.out.println(pixelsToMove);
				if (pixelsToMove != 0) {
					scrollHandle.move(pixelsToMove);
					dragStart = e.getX();
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dragging = false;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		updateHighlight(e.getPoint());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dragStart = e.getX();
		updateHighlight(e.getPoint());
	}

	private void updateHighlight(Point cursor) {
		boolean prev = scrolling;
		if (scrollRequired) {
			scrolling = scrollHandle.isCursorOver(cursor, parent);
		} else {
			scrolling = false;
		}

		if (prev != scrolling && !dragging) {
			SwingUtilities.invokeLater(parent::repaint);
		}
	}
}
