package com.univocity.trader.chart.charts.ruler;

import com.univocity.trader.chart.gui.*;

import java.awt.*;

public final class DrawingProfile {

	public enum Profile {
		DEFAULT, HIGHLIGHT, SELECTION
	}

	private Stroke stroke = new BasicStroke(1);
	private Color lineColor = new Color(233, 233, 233);
	private Color fontColor = new Color(190, 190, 190);
	private Font font = new Font("Arial", Font.PLAIN, 10);

	private Color profitBackground = new Color(150, 222, 150);
	private Color lossBackground = new Color(255, 150, 150);

	private int fontHeight;

	public DrawingProfile() {
	}

	public Stroke getStroke() {
		return stroke;
	}

	public DrawingProfile setStroke(Stroke stroke) {
		this.stroke = stroke;
		return this;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public DrawingProfile setLineColor(Color lineColor) {
		this.lineColor = lineColor;
		return this;
	}

	public Color getFontColor() {
		return fontColor;
	}

	public DrawingProfile setFontColor(Color fontColor) {
		this.fontColor = fontColor;
		return this;
	}

	public Font getFont() {
		return font;
	}

	public DrawingProfile setFont(Font font) {
		if (this.font != font) {
			this.font = font;
			fontHeight = 0;
		}
		return this;
	}

	public int getFontHeight() {
		return fontHeight;
	}

	public int getFontAscent(Graphics2D g) {
		return GraphicUtils.getFontAscent(font, g);
	}

	public DrawingProfile setFontHeight(int fontHeight) {
		this.fontHeight = fontHeight;
		return this;
	}

	public void updateFontSize(Graphics2D g) {
		if (fontHeight == 0) {
			fontHeight = GraphicUtils.getFontHeight(font, g);
		}
	}

	public DrawingProfile setProfitBackground(Color profitBackground) {
		this.profitBackground = profitBackground;
		return this;
	}

	public DrawingProfile setLossBackground(Color lossBackground) {
		this.lossBackground = lossBackground;
		return this;
	}

	public Color getProfitBackground() {
		return profitBackground;
	}

	public Color getLossBackground() {
		return lossBackground;
	}

	public void text(Graphics2D g) {
		g.setFont(font);
		g.setColor(fontColor);
	}

	public void drawing(Graphics2D g) {
		g.setColor(lineColor);
		g.setStroke(stroke);
	}

	public int getStringWidth(String str, Graphics2D g) {
		Font originalFont = g.getFont();
		g.setFont(font);

		int width = GraphicUtils.getStringWidth(str, g);
		g.setFont(originalFont);
		return width;
	}
}
