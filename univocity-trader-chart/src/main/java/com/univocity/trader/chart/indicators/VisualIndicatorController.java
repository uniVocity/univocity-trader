package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.dynamic.*;

import javax.swing.*;

@UIBound
public class VisualIndicatorController implements Theme {

	private JPanel controlPanel;
	final VisualIndicator indicator;

	public VisualIndicatorController(VisualIndicator indicator) {
		this.indicator = indicator;
	}

	@Override
	public JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = PanelBuilder.createPanel(this);
		}
		return controlPanel;
	}

	@Override
	public void invokeRepaint() {
		if(indicator.chart != null){
			indicator.chart.invokeRepaint();
		}
	}
}
