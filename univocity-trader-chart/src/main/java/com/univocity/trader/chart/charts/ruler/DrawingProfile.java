package com.univocity.trader.chart.charts.ruler;

import java.awt.*;

public interface DrawingProfile {
	enum Profile {
		DEFAULT, SELECTION
	}

	Stroke getStroke();

	void setStroke(Stroke stroke);

	Color getLineColor();

	void setLineColor(Color lineColor);

	Color getFontColor();

	void setFontColor(Color fontColor);

	Font getFont();

	void setFont(Font font);

	int getFontHeight();

	int getFontAscent(Graphics2D g);

	void updateFontSize(Graphics2D g);

	void text(Graphics2D g);

	void drawing(Graphics2D g);

	int getStringWidth(String str, Graphics2D g);
}
