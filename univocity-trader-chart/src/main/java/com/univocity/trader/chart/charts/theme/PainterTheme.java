package com.univocity.trader.chart.charts.theme;


import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

@UIBound
public class PainterTheme<T extends Repaintable> implements Theme {

	@CheckBoxBound("Logarithmic scale")
	private boolean displayingLogarithmicScale = true;

	@Label("Background color")
	@ColorBound()
	private Color backgroundColor = new Color(255, 255, 255);

	@CheckBoxBound("Anti-aliasing")
	private boolean isAntialiased = true;

	@Label("Bar width")
	@SpinnerBound(maximum = 20)
	private int barWidth = 1;

	@Label("Bar spacing")
	@SpinnerBound(minimum = 1, maximum = 20)
	private int spaceBetweenBars = 1;

	private Stroke normalStroke = new BasicStroke(1);

	@Label("Selection color")
	@ColorBound()
	private Color selectionLineColor = new Color(220, 220, 255, 150);

	@CheckBoxBound("Horizontal selection")
	private boolean horizontalSelectionLineEnabled = true;

	@CheckBoxBound("Vertical selection")
	private boolean verticalSelectionLineEnabled = true;

	@Label("Line stroke")
	@SpinnerBound(maximum = 10)
	private int stroke = 1;

	private JPanel controlPanel;

	protected T chart;

	public PainterTheme(T chart) {
		this.chart = chart;
	}

	public T getChart() {
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
		menu.addActionListener(e -> setDisplayingLogarithmicScale(isItemForLogarithmic));
		menu.setEnabled(isDisplayingLogarithmicScale() != isItemForLogarithmic);
		return menu;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public JPanel getThemeSettingsPanel() {
		if (controlPanel == null) {
			controlPanel = PanelBuilder.createPanel(this);
		}
		return controlPanel;
	}

	public int getBarWidth() {
		return barWidth;
	}

	public int getSpaceBetweenBars() {
		return spaceBetweenBars;
	}

	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
	}

	public void setSpaceBetweenBars(int spaceBetweenBars) {
		this.spaceBetweenBars = spaceBetweenBars;
	}

	public Color getSelectionLineColor() {
		return selectionLineColor;
	}

	public void setSelectionLineColor(Color selectionLineColor) {
		this.selectionLineColor = selectionLineColor;
	}

	public boolean isHorizontalSelectionLineEnabled() {
		return horizontalSelectionLineEnabled;
	}

	public void setHorizontalSelectionLineEnabled(boolean horizontalSelectionLineEnabled) {
		this.horizontalSelectionLineEnabled = horizontalSelectionLineEnabled;
	}

	public boolean isVerticalSelectionLineEnabled() {
		return verticalSelectionLineEnabled;
	}

	public void setVerticalSelectionLineEnabled(boolean verticalSelectionLineEnabled) {
		this.verticalSelectionLineEnabled = verticalSelectionLineEnabled;
	}

	public Stroke getNormalStroke() {
		return normalStroke;
	}

	public void setNormalStroke(Stroke normalStroke) {
		this.normalStroke = normalStroke;
	}

	public int getStroke() {
		return stroke;
	}

	public void setStroke(int stroke) {
		this.stroke = stroke;
		this.setNormalStroke(new BasicStroke(stroke));
	}

	@Override
	public final void invokeRepaint(){
		if(chart != null) {
			chart.invokeRepaint();
		}
	}
}

