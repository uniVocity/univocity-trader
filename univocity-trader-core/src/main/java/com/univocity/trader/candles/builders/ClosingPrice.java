package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface ClosingPrice<T, F extends CommonParserSettings<?>> {
	Volume<T, F> closingPrice(T column);
}