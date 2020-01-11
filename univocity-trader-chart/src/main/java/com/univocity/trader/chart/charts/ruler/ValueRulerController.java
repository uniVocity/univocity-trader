package com.univocity.trader.chart.charts.ruler;

import com.univocity.trader.chart.annotation.*;

import java.awt.*;

@Border("Value ruler settings")
@UIBoundClass(updateProcessor = RulerUpdateProcessor.class)
public class ValueRulerController extends RulerController<ValueRuler> {

	private int tagWidth = 0;
	private int lineWidth = 5;
	private int leftValueTagSpacing = 2;
	private int rightValueTagSpacing = 2;
	private int minRulerWidth = 50;

	public ValueRulerController(ValueRuler ruler) {
		super(ruler);
	}

	public int getTagWidth() {
		return tagWidth;
	}

	public void setTagWidth(int tagWidth) {
		this.tagWidth = tagWidth;
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	public int getLeftValueTagSpacing() {
		return leftValueTagSpacing;
	}

	public void setLeftValueTagSpacing(int leftValueTagSpacing) {
		this.leftValueTagSpacing = leftValueTagSpacing;
	}

	public int getRightValueTagSpacing() {
		return rightValueTagSpacing;
	}

	public void setRightValueTagSpacing(int rightValueTagSpacing) {
		this.rightValueTagSpacing = rightValueTagSpacing;
	}

	public int getMaxStringWidth(String str, Graphics2D g) {
		int max = super.getMaxStringWidth(str, g);
		//FIXME: check how to get a calculated value instead of 5 
		updateTagWidth(max + 5);
		return max;
	}
	
	private void updateTagWidth(int stringWidth) {
		if ((tagWidth - rightValueTagSpacing - leftValueTagSpacing) < stringWidth) {
			tagWidth = stringWidth + rightValueTagSpacing + leftValueTagSpacing;
			minRulerWidth = tagWidth + lineWidth;			
		}		
	}
	
	public int getMinimumWidth() {
		return minRulerWidth;
	}
}
