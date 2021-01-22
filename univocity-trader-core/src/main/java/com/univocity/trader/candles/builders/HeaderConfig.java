
package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface HeaderConfig<T, F extends CommonParserSettings<?>> {
	DateTimeFormat<T, F> withHeaderRow();

	DateTimeFormat<T, F> noHeaderRow();
}