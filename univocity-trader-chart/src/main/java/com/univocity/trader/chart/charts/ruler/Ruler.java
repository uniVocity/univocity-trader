package com.univocity.trader.chart.charts.ruler;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;

public abstract class Ruler<C extends RulerController<?>> implements Painter<C> {

	protected final Insets insets = new Insets(0, 0, 0, 0);
	protected final BasicChart<?> chart;
	private C controller;

	public Ruler(BasicChart<?> chart) {
		this.chart = chart;
		chart.register(this);
	}

	private void updateFontSize(Graphics2D g) {
		getController().updateFontSize(g);
	}

	public final void paintOn(Graphics2D g, int width) {
		updateFontSize(g);

		drawBackground(g, width);

		Candle candle = chart.getCurrentCandle();
		Point location = chart.getCurrentCandleLocation();

		if (candle != null && location != null) {
			drawSelection(g, width, candle, location);
		}

		highlightMousePosition(g, width);
	}

	@Override
	public final Insets insets() {
		return insets;
	}

	protected abstract void drawBackground(Graphics2D g, int width);

	protected abstract void drawSelection(Graphics2D g, int width, Candle selectedCandle, Point location);

	protected abstract void highlightMousePosition(Graphics2D g, int width);

	public final C getController() {
		if (controller == null) {
			controller = newController();
		}
		return controller;
	}

	protected abstract C newController();

	protected final int getFontHeight() {
		return getController().getFontHeight();
	}

	protected final boolean isShowingGrid() {
		return getController().isShowingGrid();
	}

	protected Color getGridColor() {
		return getController().getGridColor();
	}

	protected final void setProfile(DrawingProfile.Profile profile) {
		getController().setProfile(profile);
	}

	protected final int getStringWidth(String str, Graphics2D g) {
		return getController().getStringWidth(str, g);
	}

	protected final void text(Graphics2D g) {
		getController().text(g);
	}

	protected final void drawing(Graphics2D g) {
		getController().drawing(g);
	}

	protected Color getBackgroundColor() {
		return getController().getBackgroundColor();
	}

	protected final Color getProfitBackground() {
		return getController().getProfitBackground();
	}

	protected final Color getLossBackground() {
		return getController().getLossBackground();
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
		g.drawString(string, x + stroke * 2, y + getFontHeight() - stroke * 2);
	}

}
