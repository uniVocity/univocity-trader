package com.univocity.trader.chart.charts;

import com.univocity.trader.chart.charts.scrolling.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

public class ChartCanvas extends JPanel {

	final Insets insets = new Insets(0, 0, 0, 0);
	protected ScrollBar scrollBar;

	private boolean isPanelBeingShown = false;
	private boolean boundsChanged = false;

	protected int height;
	protected int width;
	private int requiredWidth = -1;
	private int barWidth;

	private List<StaticChart<?>> charts = new ArrayList<>();
	private List<IntConsumer> scrollPositionListeners = new ArrayList<>();
	private boolean repainting;

	public ChartCanvas() {
		this.setLayout(null);
		this.setOpaque(true);

		this.addHierarchyListener(e -> {
			if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) != 0 && isShowing()) {
				isPanelBeingShown = true;
				height = getHeight();
				width = getWidth();
			}
		});

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				boundsChanged = true;
			}
		});
	}

	public void addChart(StaticChart<?> chart) {
		this.charts.add(chart);
	}

	public void paintComponent(Graphics g1d) {
		super.paintComponent(g1d);
		updateLayout();

		Graphics2D g = (Graphics2D) g1d;


		for (StaticChart<?> chart : charts) {
			this.requiredWidth = chart.calculateRequiredWidth();
			this.barWidth = chart.calculateBarWidth();
		}

		updateScroll();

		for (StaticChart<?> chart : charts) {
			chart.paintComponent(g);
		}

		if (scrollBar != null) {
			scrollBar.draw(g);
		}
	}

	public void updateLayout() {
		if (isPanelBeingShown || boundsChanged) {
			height = getHeight();
			width = getWidth();
			for (StaticChart<?> chart : charts) {
				chart.layoutComponents();
			}

			boundsChanged = false;
		}
	}

	public int getRequiredWidth() {
		return requiredWidth;
	}

	public void enableScrolling() {
		if (scrollBar == null) {
			scrollBar = new ScrollBar(this);
			scrollPositionListeners.forEach(l -> scrollBar.addScrollPositionListener(l));
		}
	}


	public final boolean isDraggingScroll() {
		return isScrollingView() && scrollBar.isDraggingScroll();
	}

	protected final boolean isScrollingView() {
		if (scrollBar == null) {
			return false;
		}
		return scrollBar.isScrollingView();
	}

	public int getScrollHeight() {
		return scrollBar != null ? scrollBar.getHeight() : 0;
	}

	void updateScroll() {
		if (scrollBar != null) {
			scrollBar.updateScroll();
		}
	}

	public int getBoundaryRight() {
		if (scrollBar != null && scrollBar.isScrollingView()) {
			return scrollBar.getBoundaryRight();
		}
		return getWidth();
	}


	public int getBoundaryLeft() {
		if (scrollBar != null && scrollBar.isScrollingView()) {
			return scrollBar.getBoundaryLeft();
		}
		return 0;
	}

	public int translateX(int x) {
		if (scrollBar != null && scrollBar.isScrollingView()) {
			return x + scrollBar.getBoundaryLeft();
		}
		return x;
	}

	public boolean isOverDisabledSectionAtRight(int width, int x) {
		return x >= width - (insets.right + getBarWidth() * 1.5);
	}

	public boolean inDisabledSection(Point point) {
		return point.y < getHeight() - scrollBar.getHeight() && (isOverDisabledSectionAtRight(getWidth(), point.x) || point.x < insets.left + getBarWidth());
	}

	public int getBarWidth() {
		return barWidth;
	}

	public int getInsetsWidth() {
		return insets.left + insets.right;
	}

	@Override
	public Graphics2D getGraphics() {
		return (Graphics2D) super.getGraphics();
	}

	public final void invokeRepaint() {
		if (repainting) {
			return;
		}
		repainting = true;
		SwingUtilities.invokeLater(this::repaint);
	}

	public void repaint() {
		super.repaint();
		repainting = false;
	}

	public void addScrollPositionListener(IntConsumer scrollPositionListener) {
		scrollPositionListeners.add(scrollPositionListener);
	}
}
