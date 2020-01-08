package com.univocity.trader.chart;

import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;

import java.awt.*;

@Border("Chart settings")
public class InteractiveChartController extends BasicChartController {

	@Label("Selection color")
	@ColorBound()
	private Color selectionLineColor = new Color(220, 220, 255);

	@CheckBoxBound("Horizontal selection")
	private boolean horizontalSelectionLineEnabled = true;

	@CheckBoxBound("Vertical selection")
	private boolean verticalSelectionLineEnabled = true;

	public InteractiveChartController(InteractiveChart c) {
		super(c);
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

	public void copySettings(InteractiveChartController controller) {
		super.copySettings(controller);
		this.selectionLineColor = controller.selectionLineColor;
		this.horizontalSelectionLineEnabled = controller.horizontalSelectionLineEnabled;
		this.verticalSelectionLineEnabled = controller.verticalSelectionLineEnabled;
	}
}
