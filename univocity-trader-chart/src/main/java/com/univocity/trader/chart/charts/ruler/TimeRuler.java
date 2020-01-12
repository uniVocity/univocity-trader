package com.univocity.trader.chart.charts.ruler;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.indicators.base.*;

import java.awt.*;
import java.time.*;

import static com.univocity.trader.chart.charts.ruler.DrawingProfile.Profile.*;


public class TimeRuler extends Ruler<TimeRulerController> {

	private static final long ONE_DAY = TimeInterval.days(1).ms;

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

		double columnWidth = Math.round(chart.getHorizontalIncrement() * 20.0);

		double increments = width / columnWidth;

		LocalDateTime previousTime = null;
		for (int i = 0; i <= increments; i++) {
			int x = (int) Math.round(i * columnWidth);

			if (isShowingGrid()) {
				g.setColor(getGridColor());
				g.drawLine(x, 0, x, chart.getHeight());
			}


			Candle candle = chart.getCandleAtCoordinate(x);
			if (candle == null) {
				continue;
			}

			LocalDateTime candleTime = candle.localCloseDateTime();
			String text = getTimeLabel(previousTime, candleTime, candle);

			previousTime = candleTime;

			int stringWidth = getStringWidth(text, g);
			int position = getTextPosition(x, stringWidth, false);
			text(g);
			drawString(position, chart.getHeight() - getFontHeight() - chart.getScrollHeight(), text, g, 1);
		}

	}

	private String getTimeLabel(LocalDateTime previousTime, LocalDateTime candleTime, Candle candle) {
		if (previousTime == null) {
			return candle.getFormattedCloseTime("yyyy MMM dd");
		}
		long prevYear = previousTime.getYear();
		long curYear = candleTime.getYear();
		if (prevYear != curYear) {
			return candle.getFormattedCloseTime("yyyy");
		} else {
			Month prevMonth = previousTime.getMonth();
			Month curMonth = candleTime.getMonth();
			if (prevMonth != curMonth) {
				return candle.getFormattedCloseTime("MMM");
			} else {
				int prevDay = previousTime.getDayOfMonth();
				int curDay = candleTime.getDayOfMonth();
				if (prevDay != curDay) {
					return candle.getFormattedCloseTime("MMM dd HH:mm");
				} else {
					return candle.getFormattedCloseTime("HH:mm");
				}
			}
		}
	}

	protected void drawSelection(Graphics2D g, int width, Candle candle, Point location) {
		getController().setProfile(SELECTION);
		getController().drawing(g);
		String text = candle.getFormattedCloseTime("MMM dd, HH:mm");
		int stringWidth = getStringWidth(text, g) + 3;
		g.setColor(getController().getBackgroundColor());

		int position = getTextPosition(location.x, stringWidth, true);
		drawStringInBox(position, chart.getHeight() - getFontHeight() - chart.getScrollHeight(), stringWidth, text, g, 1, getBackgroundColor());
	}

	private int getTextPosition(int x, int stringWidth, boolean centralize) {
		int rightLimit = chart.getBoundaryRight() - chart.getInsetsWidth() - stringWidth;

		int position = centralize ? x - (stringWidth / 2) : x;

		if (centralize) {
			if (position <= chart.getBoundaryLeft()) {
				position = chart.getBoundaryLeft();
			} else if (position + stringWidth >= rightLimit) {
				position = rightLimit - stringWidth;
			}
		}
		return position;
	}

	public TimeRulerController newController() {
		return new TimeRulerController(this);
	}

	@Override
	public Z getZ() {
		return Z.BACK;
	}
}
