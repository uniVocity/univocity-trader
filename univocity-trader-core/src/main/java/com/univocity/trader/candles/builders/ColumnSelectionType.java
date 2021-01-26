package com.univocity.trader.candles.builders;

import com.univocity.parsers.common.*;
import com.univocity.trader.candles.*;

public interface ColumnSelectionType<F extends CommonParserSettings<?>> {
	HeaderConfig<Integer, F> selectColumnsByIndex();

	DateTimeFormat<String, F> selectColumnsByName();
}












