package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import static com.univocity.trader.indicators.Signal.*;

public class InstantaneousTrendline extends SingleValueIndicator {

	private final CircularList price;
	private static final double[] instants = Indicator.populateInstants(5000);
	private CircularList phase = new CircularList(instants.length);
	private CircularList value1 = new CircularList(10);
	private CircularList value2 = new CircularList(10);
	private CircularList value3 = new CircularList(10);
	private CircularList value5 = new CircularList(10);
	private CircularList value11 = new CircularList(10);
	private CircularList inPhase = new CircularList(10);
	private CircularList quadrature = new CircularList(10);
	private CircularList deltaPhase = new CircularList(40);
	private CircularList instPeriod = new CircularList(40);
	private Signal currentTrend;

	@Override
	protected Indicator[] children() {
		return new Indicator[0];
	}

	private static final double I_MULT = .635;
	private static final double Q_MULT = .338;

	private double trendLine;
	private double zl;
	private final boolean useHilbertTransform;

	public InstantaneousTrendline(TimeInterval interval) {
		this(interval, true);
	}

	public InstantaneousTrendline(TimeInterval interval, boolean useHilbertTransform) {
		super(interval, InstantaneousTrendline::getDetrendPrice);
		price = new CircularList(instants.length);
		this.useHilbertTransform = useHilbertTransform;
	}

	@Override
	public double getValue() {
		return 0;
	}


	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (updating) {
			price.update(value);
		} else {
			price.add(value);
		}
		currentTrend = this.calculate(updating);

		return true;
	}

	@Override
	protected Signal calculateSignal(Candle candle) {
		return currentTrend;
	}

	private static double getDetrendPrice(Candle candle) {
		return (candle.high + candle.low) / 2.0;
	}

	private Signal calculate(boolean updating) {
		if (getAccumulationCount() <= 6) {
			return NEUTRAL;
		}

		// Compute InPhase and Quadrature components
		if (useHilbertTransform) {
			value1.accumulate(price.getRecentValue(1) - price.getRecentValue(7), updating);
			value2.accumulate(value1.getRecentValue(4), updating);
			value3.accumulate(.75 * (value1.getRecentValue(1) - value1.getRecentValue(7)) + .25 * (value1.getRecentValue(3) - value1.getRecentValue(5)), updating);
			inPhase.accumulate(.33 * value2.getRecentValue(1) + .67 * inPhase.getRecentValue(2), updating);
			quadrature.accumulate(.2 * value3.getRecentValue(1) + .8 * quadrature.getRecentValue(2), updating);
		} else {
			value3.accumulate(price.getRecentValue(1) - price.getRecentValue(8), updating); // Detrend Price
			inPhase.accumulate(1.25 * (value3.getRecentValue(5) - I_MULT * value3.getRecentValue(3)) + I_MULT * inPhase.getRecentValue(4), updating);
			quadrature.accumulate(value3.getRecentValue(3) - Q_MULT * value3.getRecentValue(1) + Q_MULT * quadrature.getRecentValue(3), updating);
		}

		// Use ArcTangent to compute the current phase
		if (Math.abs(inPhase.getRecentValue(1) + inPhase.getRecentValue(2)) > 0) {
			double a = Math.abs((quadrature.getRecentValue(1) + quadrature.getRecentValue(2) / (inPhase.getRecentValue(1) + inPhase.getRecentValue(2))));
			phase.accumulate(Math.atan(a), updating);
		}

		// Resolve the ArcTangent ambiguity
		if (inPhase.getRecentValue(1) < 0 && quadrature.getRecentValue(1) > 0) {
			phase.accumulate(180 - phase.getRecentValue(1), updating);
		}
		if (inPhase.getRecentValue(1) < 0 && quadrature.getRecentValue(1) < 0) {
			phase.accumulate(180 + phase.getRecentValue(1), updating);
		}
		if (inPhase.getRecentValue(1) > 0 && quadrature.getRecentValue(1) < 0) {
			phase.accumulate(360 - phase.getRecentValue(1), updating);
		}

		// Compute a differential phase, resolve phase wraparound, and limit delta phase errors
		deltaPhase.accumulate(phase.getRecentValue(2) - phase.getRecentValue(1), updating);

		if (phase.getRecentValue(2) < 90 && phase.getRecentValue(1) > 270) {
			deltaPhase.accumulate(360 + phase.getRecentValue(2) - phase.getRecentValue(1), updating);
		}
		if (deltaPhase.getRecentValue(1) < 1) {
			deltaPhase.accumulate(1, updating);
		}
		if (deltaPhase.getRecentValue(1) > 60) {
			deltaPhase.accumulate(60, updating);
		}

		// Sum DeltaPhases to reach 360 degrees. The sum is the instantaneous period.
		double value4 = 0;
		for (int count = 0; count < 40; count++) {
			value4 = value4 + deltaPhase.getRecentValue(count + 1);
			if (value4 > 360 && instPeriod.getRecentValue(1) == 0) {
				instPeriod.accumulate(count, updating);
			}
		}


		// Resolve Instantaneous Period errors and smooth
		if (instPeriod.getRecentValue(1) == 0) {
			instPeriod.accumulate(instPeriod.getRecentValue(2), updating);
		}
		value5.accumulate(.25 * instPeriod.getRecentValue(1) + .75 * value5.getRecentValue(2), updating);

		// Compute Trendline as simple average over the measured dominant cycle period
		long period = (int) value5.getRecentValue(1);
		double trendline = 0;
		for (int count = 0; count < period + 1; count++) {
			trendline = trendline + price.getRecentValue(count + 1);
		}
		if (period > 0) {
			trendline = trendline / (period + 2);
		}

		value11.accumulate(.33 * (price.getRecentValue(1) + .5 * (price.getRecentValue(1) - price.getRecentValue(4))) + .67 * value11.getRecentValue(2), updating);

		this.trendLine = trendline;
		this.zl = value11.getRecentValue(1);

		if (getAccumulationCount() < 26) {
			return NEUTRAL;
		}

		return zl < trendLine ? BUY : SELL;
	}

	public double getTrendLine() {
		return trendLine;
	}

	public double getZl() {
		return zl;
	}
}
