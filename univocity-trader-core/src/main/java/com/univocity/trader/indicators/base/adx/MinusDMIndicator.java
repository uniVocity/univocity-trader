package com.univocity.trader.indicators.base.adx;

import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MinusDMIndicator extends AbstractDMIndicator {

	public MinusDMIndicator(TimeInterval interval) {
		super(interval);
	}

	@Override
	protected double calculate(double upMove, double downMove) {
		return downMove > upMove && downMove > 0 ? downMove : 0;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
