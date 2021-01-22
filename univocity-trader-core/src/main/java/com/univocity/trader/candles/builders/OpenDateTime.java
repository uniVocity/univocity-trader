package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface OpenDateTime<T, F extends CommonParserSettings<?>> {
	CloseDateTime<T, F> openDateTime(T column);
}