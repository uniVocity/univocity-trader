package com.univocity.trader.chart.charts.ruler;

import com.univocity.trader.chart.gui.*;

import java.awt.*;

public class DrawingProfileImpl implements DrawingProfile {

	private Stroke stroke;
	private Color lineColor;

	private Color fontColor;
	private Font font;
	private int fontHeight;

	public DrawingProfileImpl(Stroke stroke, Color lineColor, Font font, Color fontColor) {
		this.stroke = stroke;
		this.lineColor = lineColor;
		this.font = font;
		this.fontColor = fontColor;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getFontColor() {
		return fontColor;
	}

	public void setFontColor(Color fontColor) {
		this.fontColor = fontColor;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		if (this.font != font) {
			this.font = font;
			fontHeight = 0;
		}
	}

	public int getFontHeight() {
		return fontHeight;
	}

	public int getFontAscent(Graphics2D g) {
		return GraphicUtils.getFontAscent(font, g);
	}

	public void setFontHeight(int fontHeight) {
		this.fontHeight = fontHeight;
	}

	public void updateFontSize(Graphics2D g) {
		if (fontHeight == 0) {
			fontHeight = GraphicUtils.getFontHeight(font, g);
		}
	}

	public void text(Graphics2D g) {
		g.setFont(font);
		g.setColor(fontColor);
	}

	public void drawing(Graphics2D g) {
		g.setColor(lineColor);
		g.setStroke(stroke);
	}

	@Override
	public int getStringWidth(String str, Graphics2D g) {
		Font originalFont = g.getFont();
		g.setFont(font);

		int width = GraphicUtils.getStringWidth(str, g);
		g.setFont(originalFont);
		return width;
	}
}
