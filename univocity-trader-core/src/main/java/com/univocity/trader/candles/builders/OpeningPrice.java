package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface OpeningPrice<T, F extends CommonParserSettings<?>> {
	HighestPrice<T, F> openingPrice(T column);

	HighestPrice<T, F> noOpeningPrice();
}