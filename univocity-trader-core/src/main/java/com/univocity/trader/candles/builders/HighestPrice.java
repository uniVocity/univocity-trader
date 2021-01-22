package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface HighestPrice<T, F extends CommonParserSettings<?>> {
	LowestPrice<T, F> highestPrice(T column);

	LowestPrice<T, F> noHighestPrice();
}