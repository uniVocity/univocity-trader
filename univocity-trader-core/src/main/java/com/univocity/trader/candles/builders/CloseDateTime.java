package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface CloseDateTime<T, F extends CommonParserSettings<?>> {
	OpeningPrice<T, F> closeDateTime(T column);

	OpeningPrice<T, F> noCloseDateTime();
}
