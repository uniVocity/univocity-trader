package com.univocity.trader.indicators.base.adx;

import com.univocity.trader.indicators.base.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class PlusDIIndicator extends AbstractDIIndicator {

	public PlusDIIndicator(int length, TimeInterval interval) {
		super(length, interval);
	}

	@Override
	protected AbstractDMIndicator getDMIndicator(TimeInterval interval) {
		return new PlusDMIndicator(interval);
	}
}
