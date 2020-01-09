package com.univocity.trader.chart.charts.controls;


import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

@UIBoundClass(updateProcessor = ChartUpdateProcessor.class)
public class BasicChartController implements Controller {

	@CheckBoxBound("Logarithmic scale")
	private boolean displayingLogarithmicScale = true;

	@Label("Background color")
	@ColorBound()
	private Color backgroundColor = Color.WHITE;

	@CheckBoxBound("Anti-aliasing")
	private boolean isAntialiased = true;

	@Label("Bar width")
	@SpinnerBound(maximum = 20)
	private int candleWidth = 1;

	@Label("Bar spacing")
	@SpinnerBound(minimum = 1, maximum = 20)
	private int spaceBetweenCandles = 0;

	private JPanel controlPanel;

	protected BasicChart chart;

	public BasicChartController(BasicChart chart) {
		this.chart = chart;
	}

	public BasicChart getChart() {
		return chart;
	}

	public boolean isAntialiased() {
		return isAntialiased;
	}

	public void setIsAntialiased(boolean isAntialiased) {
		this.isAntialiased = isAntialiased;
	}

	public boolean isDisplayingLogarithmicScale() {
		return displayingLogarithmicScale;
	}

	public void setDisplayingLogarithmicScale(boolean displayingLogarithmicScale) {
		if (this.displayingLogarithmicScale != displayingLogarithmicScale) {
			this.displayingLogarithmicScale = displayingLogarithmicScale;
		}
	}

	public List<JMenu> getMenuOptions() {
		ArrayList<JMenu> options = new ArrayList<JMenu>();
		return options;
	}

	protected JMenu getScaleMenu() {
		JMenu appearanceMenu = new JMenu("Scale");
		appearanceMenu.add(getLinearScaleMenuItem());
		appearanceMenu.add(getLogarithmicScaleMenuItem());
		return appearanceMenu;
	}

	protected JMenuItem getLinearScaleMenuItem() {
		return createScaleMenuItem("Linear", false);
	}

	protected JMenuItem getLogarithmicScaleMenuItem() {
		return createScaleMenuItem("Logarithmic", true);
	}

	private JMenuItem createScaleMenuItem(String text, final boolean isItemForLogarithmic) {
		JMenuItem menu = new JMenuItem(text);
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDisplayingLogarithmicScale(isItemForLogarithmic);
			}
		});
		menu.setEnabled(isDisplayingLogarithmicScale() != isItemForLogarithmic);
		return menu;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = PanelBuilder.createPanel(this);
		}
		return controlPanel;
	}

	public int getCandleWidth() {
		return candleWidth;
	}

	public int getSpaceBetweenCandles() {
		return spaceBetweenCandles;
	}

	public void setCandleWidth(int candleWidth) {
		this.candleWidth = candleWidth;
	}

	public void setSpaceBetweenCandles(int spaceBetweenCandles) {
		this.spaceBetweenCandles = spaceBetweenCandles;
	}
}

