package com.univocity.trader.chart.charts.scrolling;

import com.univocity.trader.chart.charts.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ScrollBar extends MouseAdapter {

	private static final Color glassBlue = new Color(0, 0, 255, 128);
	private static final Color barGray = new Color(128, 128, 128, 64);
	private final Point gradientStart = new Point(0, 0);
	private final Point gradientEnd = new Point(0, 0);

	final ChartCanvas canvas;

	int height = 10;
	boolean scrolling;
	private boolean scrollRequired;
	private boolean dragging;
	private int dragStart;
	private double scrollStep;

	final ScrollHandle scrollHandle = new ScrollHandle(this);

	public ScrollBar(ChartCanvas canvas) {
		this.canvas = canvas;
		canvas.addMouseMotionListener(this);

		Timer timer = new Timer(500, (e) -> {
			if (!dragging) {
				Point p = MouseInfo.getPointerInfo().getLocation();
				p = new Point(p.x - canvas.getLocation().x, p.y - canvas.getLocation().y);
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
		double required = canvas.getRequiredWidth();
		double available = canvas.getWidth();

		double scrollingArea = available < ScrollHandle.MIN_WIDTH ? ScrollHandle.MIN_WIDTH : available;
		double handleWidth = scrollingArea * (available / required);
		scrollStep = (required - available) / (available - handleWidth);
		scrollHandle.setWidth((int) handleWidth);

		scrollRequired = required > available;
	}

	public void draw(Graphics2D g) {

		gradientStart.x = canvas.getWidth() / 2;
		gradientStart.y = -50;
		gradientEnd.x = canvas.getWidth() / 2;
		gradientEnd.y = height + 50;

		g.setPaint(new GradientPaint(gradientStart, glassBlue, gradientEnd, barGray));
		g.fillRect(0, canvas.getHeight() - height, canvas.getWidth(), height);

		if (scrollRequired) {
			scrollHandle.draw(g, canvas);
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
			scrolling = scrollHandle.isCursorOver(cursor, canvas);
		} else {
			scrolling = false;
		}

		if (prev != scrolling && !dragging) {
			canvas.invokeRepaint();
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

	public void addScrollPositionListener(IntConsumer positionUpdateListener){
		scrollHandle.addScrollPositionListener(positionUpdateListener);
	}
}
