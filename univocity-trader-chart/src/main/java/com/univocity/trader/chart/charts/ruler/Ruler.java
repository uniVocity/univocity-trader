package com.univocity.trader.chart.charts.ruler;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.text.*;

public abstract class Ruler<C extends RulerController<?>> implements Painter<C> {

	protected final BasicChart<?> chart;
	private C controller;
	private BufferedImage cachedBackground = null;
	private boolean isCachedBackgroundReady = false;

	//	private final UpdateThread backgroundUpdater = new UpdateThread(){
//		{
//			this.setUpdateInterval(200);
//		}
//		
//		@Override
//		protected void update() {
	//TODO: use this code when enabling real time updates,
//			if(width <= 0 || height <= 0){
//				return;
//			}
//			cachedBackground = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//			Graphics2D g = (Graphics2D)cachedBackground.getGraphics();
//			clearGraphics(g);
//			drawBackground(g);			
//			isCachedBackgroundReady = true;
//		}
//	};


	public Ruler(BasicChart<?> chart) {
		this.chart = chart;
		chart.register(this);
		chart.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				isCachedBackgroundReady = false;
			}
		});
	}

	public final void paintOn(Graphics2D g, int width) {
		isCachedBackgroundReady &= cachedBackground != null && cachedBackground.getWidth() == width && cachedBackground.getHeight() == chart.getHeight() /*&& !controller.isCacheRefreshPending()*/;
		if (isCachedBackgroundReady) {
			g.drawImage(cachedBackground, 0, 0, width, chart.getHeight(), chart);
		} else {
			drawBackground(g, width);
			//controller.setCacheRefreshPending(false);
		}

		Candle candle = chart.getSelectedCandle();
		Point location = chart.getSelectedCandleLocation();
		if (candle != null && location != null && isCachedBackgroundReady) {
			drawSelection(g, width, candle, location);
		}
	}

	protected abstract void drawBackground(Graphics2D g, int width);

	protected abstract void drawSelection(Graphics2D g, int width, Candle selectedCandle, Point location);

	protected boolean isCachingSupported() {
		return false;
	}

	protected String readFieldFormatted(Candle candle) {
		return getValueFormat().format(chart.getCentralValue(candle));
	}

	protected abstract Format getValueFormat();

	public final C getController() {
		if (controller == null) {
			controller = newController();
		}
		return controller;
	}

	protected abstract C newController();
}
