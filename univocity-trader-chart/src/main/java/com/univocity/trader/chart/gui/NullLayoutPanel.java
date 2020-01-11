package com.univocity.trader.chart.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class NullLayoutPanel extends JPanel {

	private boolean isPanelBeingShown = false;
	private boolean boundsChanged = false;

	protected int height;
	protected int width;

	public NullLayoutPanel() {
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

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		updateLayout();
	}

	public void updateLayout() {
		if (isPanelBeingShown || boundsChanged) {
			height = getHeight();
			width = getWidth();
			layoutComponents();
			revalidate();
			boundsChanged = false;
		}
	}

	public int getRequiredWidth() {
		return -1;
	}

	protected abstract void layoutComponents();

	@Override
	public Graphics2D getGraphics() {
		return (Graphics2D) super.getGraphics();
	}


}
