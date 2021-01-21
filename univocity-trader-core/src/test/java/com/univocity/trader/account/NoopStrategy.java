package com.univocity.trader.account;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;

public class NoopStrategy implements Strategy {

	@Override
	public Signal getSignal(Candle candle, Context context) {
		return Signal.NEUTRAL;
	}
}
