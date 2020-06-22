package com.univocity.trader.chart.gui;

import java.awt.*;
import java.awt.geom.*;

public class GraphicUtils {
	public static int getFontHeight(Font font, Graphics g) {
		FontMetrics m = g.getFontMetrics(font);
		return m.getHeight();
	}

	public static int getFontAscent(Font font, Graphics g) {
		FontMetrics m = g.getFontMetrics(font);
		return m.getAscent();
	}

	public static int getStringWidth(String string, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return fm.stringWidth(string);
	}

	public static Rectangle2D getStringRect(String string, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return fm.getStringBounds(string, g);
	}

	public static void drawStringInBox(int x, int y, String string, Graphics2D g, int boxOutlineStroke, Color background, Font font, Color fontColor) {
		drawStringInBox(x, y, getStringWidth(string, g), string, g, boxOutlineStroke, background, font, fontColor);
	}

	public static void drawStringInBox(int x, int y, int width, String string, Graphics2D g, int boxOutlineStroke, Color background, Font font, Color fontColor) {
		final int height = getFontHeight(font, g);
		y -= height;
		g.setColor(background);
		g.fillRect(x, y, width, height);

		if (boxOutlineStroke > 0) {
			g.setColor(background);
			g.setStroke(new BasicStroke(boxOutlineStroke));
			g.drawRect(x, y, width, height);
		}

		g.setColor(fontColor);
		g.setFont(font);
		g.drawString(string, x + boxOutlineStroke * 2, y + height - boxOutlineStroke * 2);
	}
}
