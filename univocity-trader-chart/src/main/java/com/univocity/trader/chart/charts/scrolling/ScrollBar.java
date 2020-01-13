package com.univocity.trader.chart.charts.scrolling;

import com.univocity.trader.chart.charts.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ScrollBar extends MouseAdapter {

	private static final Color glassBlue = new Color(0, 0, 255, 128);
	private static final Color barGray = new Color(128, 128, 128, 64);
	private final Point gradientStart = new Point(0, 0);
	private final Point gradientEnd = new Point(0, 0);

	final StaticChart<?> parent;

	int height = 10;
	boolean scrolling;
	private boolean scrollRequired;
	private boolean dragging;
	private int dragStart;
	private double scrollStep;

	final ScrollHandle scrollHandle = new ScrollHandle(this);

	public ScrollBar(StaticChart<?> parent) {
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

	public boolean isScrollingView() {
		return scrollRequired;
	}

	public void updateScroll() {

		double required = parent.getRequiredWidth();
		double available = parent.getWidth();

		double scrollingArea = available < ScrollHandle.MIN_WIDTH ? ScrollHandle.MIN_WIDTH : available;
		double handleWidth = scrollingArea * (available / required);
		scrollStep = (required - available) / (available - handleWidth);
		scrollHandle.setWidth((int) handleWidth);

		scrollRequired = required > available;
	}

	public void draw(Graphics2D g) {

		gradientStart.x = parent.getWidth() / 2;
		gradientStart.y = -50;
		gradientEnd.x = parent.getWidth() / 2;
		gradientEnd.y = height + 50;

		g.setPaint(new GradientPaint(gradientStart, glassBlue, gradientEnd, barGray));
		g.fillRect(0, parent.getHeight() - height, parent.getWidth(), height);

		if (scrollRequired) {
			scrollHandle.draw(g, parent);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (scrollRequired) {
			if (scrolling) {
				dragStart = drag(e, dragStart);
			}
		}
	}

	public int drag(MouseEvent e, int dragStart){
		dragging = true;

		int pixelsToMove = e.getX() - dragStart;

		pixelsToMove = scrollHandle.getMovablePixels(pixelsToMove);
		if (pixelsToMove != 0) {
			scrollHandle.move(pixelsToMove);
			dragStart = e.getX();
		}
		return dragStart;
	}

	public int getBoundaryRight() {
		return (int) Math.round((scrollHandle.getPosition() + scrollHandle.getWidth()) * scrollStep);
	}

	public int getBoundaryLeft() {
		return (int) Math.round(scrollHandle.getPosition() * scrollStep);
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

		if (!scrollRequired) {
			scrollHandle.setPosition(Integer.MAX_VALUE);
		}
	}

	public int getHeight(){
		return height;
	}

	public boolean isDraggingScroll(){
		return scrollRequired && dragging;
	}
}
