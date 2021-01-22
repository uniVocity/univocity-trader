package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface DateTimeFormat<T, F extends CommonParserSettings<?>> {
	OpenDateTime<T, F> dateAndTimePattern(String pattern);

	OpenDateTime<T, F> dateAndTimeInMillis();
}