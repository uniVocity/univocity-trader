package com.univocity.trader.indicators.base.adx;

import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class PlusDMIndicator extends AbstractDMIndicator {

	public PlusDMIndicator(TimeInterval interval) {
		super(interval);
	}

	@Override
	protected double calculate(double upMove, double downMove) {
		return upMove > downMove && upMove > 0 ? upMove : 0;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}