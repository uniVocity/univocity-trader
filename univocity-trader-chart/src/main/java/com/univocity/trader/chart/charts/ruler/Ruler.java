package com.univocity.trader.chart.charts.ruler;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;

public abstract class Ruler<T extends RulerTheme<?>> implements Painter<T> {

	Insets insets = new Insets(0, 0, 0, 0);
	private T theme;
	private BasicChart<?> chart;
	protected Color textColor;

	public Ruler(BasicChart<?> chart) {
		chart.addPainter(overlay(), this);
	}

	private void updateFontSize(Graphics2D g) {
		this.theme().updateFontSize(g);
	}

	public final void paintOn(BasicChart<?> chart, Graphics2D g, int width, Overlay overlay) {
		updateFontSize(g);

		drawBackground(chart, g, width);

		Candle candle = chart.getCurrentCandle();
		Point location = chart.getCurrentCandleLocation();

		if (candle != null && location != null) {
			drawSelection(chart, g, width, candle, location);
		}

		highlightMousePosition(chart, g, width);
	}

	protected abstract void drawBackground(BasicChart<?> chart, Graphics2D g, int width);

	protected abstract void drawSelection(BasicChart<?> chart, Graphics2D g, int width, Candle selectedCandle, Point location);

	protected abstract void highlightMousePosition(BasicChart<?> chart, Graphics2D g, int width);

	public final T theme() {
		if (theme == null) {
			theme = newTheme();
		}
		return theme;
	}

	@Override
	public Insets insets() {
		return insets;
	}

	protected abstract T newTheme();

	protected final int getFontHeight() {
		return this.theme().getFontHeight();
	}

	protected final boolean isShowingGrid() {
		return this.theme().isShowingGrid();
	}

	protected Color getGridColor() {
		return this.theme().getGridColor();
	}

	protected final void setProfile(DrawingProfile.Profile profile) {
		this.theme().setProfile(profile);
	}

	protected final int getStringWidth(String str, Graphics2D g) {
		return this.theme().getStringWidth(str, g);
	}

	protected final void text(Graphics2D g) {
		this.theme().text(g);
	}

	protected final void drawing(Graphics2D g) {
		this.theme().drawing(g);
	}

	protected Color getBackgroundColor() {
		return this.theme().getBackgroundColor();
	}

	protected final Color getProfitBackground() {
		return this.theme().getProfitBackground();
	}

	protected final Color getLossBackground() {
		return this.theme().getLossBackground();
	}

	protected void drawStringInBox(int x, int y, int width, String string, Graphics2D g, int stroke, Color background) {
		g.setColor(background);
		g.fillRect(x, y, width, getFontHeight());
		drawing(g);
		g.setStroke(new BasicStroke(stroke));
		g.drawRect(x, y, width, getFontHeight());

		text(g);
		g.drawString(string, x + stroke * 2, y + getFontHeight() - stroke * 2);
	}

	protected void drawString(int x, int y, String string, Graphics2D g, int stroke) {
		text(g);
		if(textColor != null){
			g.setColor(textColor);
		}
		g.drawString(string, x + stroke * 2, y + getFontHeight() - stroke * 2);
	}

	@Override
	public final void invokeRepaint() {
		if (chart != null) {
			chart.invokeRepaint();
		}
	}

	@Override
	public void install(BasicChart<?> chart) {
		this.chart = chart;
	}

	@Override
	public void uninstall(BasicChart<?> chart) {
		chart = null;
	}
}
