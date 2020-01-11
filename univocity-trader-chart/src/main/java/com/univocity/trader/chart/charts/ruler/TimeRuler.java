package com.univocity.trader.chart.charts.ruler;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;

import java.awt.*;

import static com.univocity.trader.chart.charts.ruler.DrawingProfile.Profile.*;


public class TimeRuler extends Ruler<TimeRulerController> {

	public TimeRuler(BasicChart<?> chart) {
		super(chart);
	}

	@Override
	protected void drawBackground(Graphics2D g, int width) {
		getController().setProfile(DEFAULT);
		drawGrid(g, width);
	}


	private void drawGrid(Graphics2D g, int width) {
		if (chart.candleHistory.isEmpty()) {
			return;
		}
		if (isShowingGrid()) {
			g.setColor(getGridColor());

			double columnWidth = Math.round(chart.getHorizontalIncrement() * 10.0);

			double increments = width / columnWidth;

			for (int i = 1; i <= increments; i++) {
				int x = (int) Math.round(i * columnWidth);
				g.drawLine(x, 0, x, chart.getHeight());
			}
		}
	}

	protected void drawSelection(Graphics2D g, int width, Candle candle, Point location) {
		int x = location.x;

		getController().setProfile(SELECTION);
		getController().drawing(g);
		String time = candle.getFormattedCloseTime("MMM dd, HH:mm");
		int stringWidth = getController().getStringWidth(time, g) + 3;
		g.setColor(getController().getBackgroundColor());

		int rightLimit = chart.getBoundaryRight() - chart.getInsetsWidth() - stringWidth;

		int position = x - (stringWidth / 2);

		if (position <= chart.getBoundaryLeft()) {
			position = chart.getBoundaryLeft();
		} else if (position + stringWidth >= rightLimit) {
			position = rightLimit - stringWidth;
		}
		drawStringInBox(position, chart.getHeight() - getFontHeight() - chart.getScrollHeight(), stringWidth, time, g, 1);
	}

	public TimeRulerController newController() {
		return new TimeRulerController(this);
	}

	@Override
	public Z getZ() {
		return Z.BACK;
	}
}
