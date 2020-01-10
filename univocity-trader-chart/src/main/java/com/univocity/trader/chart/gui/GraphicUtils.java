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

}
