package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;

public interface Volume<T, F extends CommonParserSettings<?>> {
	Build<T, F> volume(T column);

	Build<T, F> noVolume();
}