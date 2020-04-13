package com.univocity.trader.chart.charts.scrolling;

import java.awt.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
class ScrollHandle extends Draggable {
	private int width = 0;

	private static final Color glassBlack = new Color(0, 0, 0, 128);
	private static final Color glassGray = new Color(128, 128, 128, 92);
	private static final Color glassHover = new Color(128, 128, 128, 150);
	private Point gradientStart = new Point(0, 0);
	private Point gradientEnd = new Point(0, 0);

	private final ScrollBar scrollBar;

	public ScrollHandle(ScrollBar scrollBar) {
		this.scrollBar = scrollBar;
	}

	public boolean isCursorOver(Point p, Component c) {
		return p.x >= getPosition() && p.x <= getPosition() + Math.max(width, 20) && p.y <= c.getHeight() && p.y >= c.getHeight() - scrollBar.height;
	}

	public boolean pinnedToRight() {
		return getPosition() + getWidth() >= maxPosition();
	}

	public void setWidth(int width) {
		if(this.width != width) {
			boolean pinnedToRight = pinnedToRight();
			this.width = width;
			setPosition(pinnedToRight ? maxPosition() : getPosition());
		}
	}

	public int getWidth() {
		return width;
	}

	@Override
	protected int minPosition() {
		return 0;
	}

	@Override
	protected int maxPosition() {
		return scrollBar.canvas.getWidth() - width;
	}

	public void draw(Graphics2D g, Component c) {
		int position = this.getPosition();

		int width = this.width;
		if(width < 10){
			width = 10;
		}

		gradientStart.x = position + (width);
		gradientEnd.x = position - 2;
		g.setPaint(new GradientPaint(gradientStart, glassBlack, gradientEnd, glassGray));
		g.fillRoundRect(position, c.getHeight() - scrollBar.height + 1, width, scrollBar.height - 1, 5, 5);
		gradientStart.x = position + (width);
		gradientEnd.x = position + width + 2;
		g.setPaint(new GradientPaint(gradientStart, scrollBar.scrolling? glassHover : glassGray, gradientEnd, glassBlack));
		g.fillRoundRect(position, c.getHeight() - scrollBar.height + 1, width, scrollBar.height - 1, 5, 5);


		g.setColor(Color.GRAY);
		g.drawRoundRect(position, c.getHeight() - scrollBar.height + 1, width, scrollBar.height - 2, 5, 5);
	}
}
