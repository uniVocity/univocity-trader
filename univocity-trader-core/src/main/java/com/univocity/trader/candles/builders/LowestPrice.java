package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface LowestPrice<T, F extends CommonParserSettings<?>> {
	ClosingPrice<T, F> lowestPrice(T column);

	ClosingPrice<T, F> noLowestPrice();
}
