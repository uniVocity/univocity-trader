package com.univocity.trader.indicators;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum Signal {
	UNDERVALUED('u', 1),
	SELL('S', -0.5),
	NEUTRAL('-', 0),
	BUY('B', 0.5),
	OVERVALUED('o', -1);

	public final double value;
	public final char code;

	Signal(char code, double value) {
		this.value = value;
		this.code = code;
	}

	Signal max(Signal indicator){
		if(this.value > indicator.value){
			return this;
		} else {
			return indicator;
		}
	}

	Signal min(Signal indicator){
		if(this.value < indicator.value){
			return this;
		} else {
			return indicator;
		}
	}
}
