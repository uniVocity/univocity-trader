package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;


public class WaddahAttarExplosion extends SingleValueIndicator {

	private final BollingerBand bb;
	private final MACD macd;
	private final double sensitivity;
	private double trend;
	private double newMacdValue;
	private double oldMacdValue;
	private boolean upTrend;

	public WaddahAttarExplosion(TimeInterval interval) {
		this(150, 20, 40, 20, 2.0, interval, null);
	}

	public WaddahAttarExplosion(double sensitivity, int fastLength, int slowLength, int channelLength, double multiplier, TimeInterval interval) {
		this(sensitivity, fastLength, slowLength, channelLength, multiplier, interval, null);
	}

	public WaddahAttarExplosion(double sensitivity, int fastLength, int slowLength, int channelLength, double multiplier, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		valueGetter = valueGetter == null ? c -> c.close : valueGetter;
		bb = new BollingerBand(channelLength, multiplier, interval, valueGetter);
		macd = new MACD(fastLength, slowLength, 9, interval, valueGetter);
		this.sensitivity = sensitivity;
	}


	public boolean process(Candle candle, double value, boolean updating) {
		bb.accumulate(candle);
		macd.accumulate(candle);

		oldMacdValue = newMacdValue;
		newMacdValue = macd.getValue();

		if (getAccumulationCount() == 0) {
			trend = 0;
		} else {
			trend = (newMacdValue - oldMacdValue) * sensitivity;
		}

		upTrend = (trend >= 0);
		if (!upTrend)
			trend *= -1;

		return true;
	}


	public double getTrend() {
		return trend;
	}


	public double getExplosion() {
		return bb.getUpperBand() - bb.getLowerBand();
	}

	public boolean isTrendUp() {
		return upTrend;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{bb, macd};
	}
}
