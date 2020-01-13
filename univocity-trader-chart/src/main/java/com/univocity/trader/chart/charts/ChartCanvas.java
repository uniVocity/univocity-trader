package com.univocity.trader.chart.charts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class ChartCanvas extends JPanel {

	private boolean isPanelBeingShown = false;
	private boolean boundsChanged = false;

	protected int height;
	protected int width;
	private int requiredWidth = -1;

	private List<StaticChart<?>> charts = new ArrayList<>();

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

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		updateLayout();

		for (StaticChart<?> chart : charts) {
			this.requiredWidth = Math.max(chart.calculateRequiredWidth(), requiredWidth);
		}

		for (StaticChart<?> chart : charts) {
			chart.paintComponent((Graphics2D) g);
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

	@Override
	public Graphics2D getGraphics() {
		return (Graphics2D) super.getGraphics();
	}

	public final void invokeRepaint() {
		SwingUtilities.invokeLater(this::repaint);
	}

}
