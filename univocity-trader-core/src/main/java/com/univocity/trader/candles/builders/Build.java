package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;
import com.univocity.trader.candles.*;

public interface Build<T, F extends CommonParserSettings<?>> {
	RowFormat<T, F> build();
}

