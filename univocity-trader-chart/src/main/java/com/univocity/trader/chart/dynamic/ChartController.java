package com.univocity.trader.chart.dynamic;


import com.univocity.trader.chart.*;
import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;

import java.awt.*;

@Border("Chart settings")
public class ChartController extends BasicChartController {

	@Label("Selection color")
	@ColorBound()
	private Color selectionLineColor = new Color(220, 220, 255);
	
	@CheckBoxBound("Horizontal selection")
	private boolean horizontalSelectionLineEnabled = true;
	
	@CheckBoxBound("Vertical selection")
	private boolean verticalSelectionLineEnabled = true;

	private boolean autofit = true;
	

	public ChartController(InteractiveChart c){
		super(c);
	}

	public int getSpaceBetweenUnits(){
		return 0;
	}

	public int getGraphicUnitSize(){
		return 1;
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
	
	public boolean isAutofit() {
		return autofit;
	}

	public void setAutofit(boolean autofit) {
		this.autofit = autofit;
	}

	public void copySettings(ChartController controller) {
		super.copySettings(controller);
		this.autofit = controller.autofit;
		this.selectionLineColor = controller.selectionLineColor;
		this.horizontalSelectionLineEnabled = controller.horizontalSelectionLineEnabled;
		this.verticalSelectionLineEnabled = controller.verticalSelectionLineEnabled;
	}

}
