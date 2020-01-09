package com.univocity.trader.chart;

import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;

import java.awt.*;

@Border("Chart settings")
public class InteractiveChartController extends BasicChartController {

	private Stroke normalStroke = new BasicStroke(1);

	@Label("Selection color")
	@ColorBound()
	private Color selectionLineColor = new Color(220, 220, 255, 100);

	@CheckBoxBound("Horizontal selection")
	private boolean horizontalSelectionLineEnabled = true;

	@CheckBoxBound("Vertical selection")
	private boolean verticalSelectionLineEnabled = true;

	@Label("Line stroke")
	@SpinnerBound(maximum = 10)
	private int stroke = 1;

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

}
