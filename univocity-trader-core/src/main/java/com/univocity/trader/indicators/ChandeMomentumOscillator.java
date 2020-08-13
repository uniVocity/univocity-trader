package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

public class ChandeMomentumOscillator extends SingleValueIndicator {

	private double value;

	private final GainIndicator gainIndicator;
	private final LossIndicator lossIndicator;

	private CircularList gains;
	private CircularList losses;

	public ChandeMomentumOscillator(TimeInterval interval) {
		this(9, interval);
	}

	public ChandeMomentumOscillator(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public ChandeMomentumOscillator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		gainIndicator = new GainIndicator(interval, valueGetter);
		lossIndicator = new LossIndicator(interval, valueGetter);

		gains = new CircularList(length);
		losses = new CircularList(length);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (gainIndicator.accumulate(candle)) {
			lossIndicator.accumulate(candle);

			gains.accumulate(gainIndicator.getValue(), updating);
			losses.accumulate(lossIndicator.getValue(), updating);

			this.value = ((gains.sum() - losses.sum()) / (gains.sum() + losses.sum())) * 100.0;
			return true;
		}
		return false;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{gainIndicator, lossIndicator};
	}

}
