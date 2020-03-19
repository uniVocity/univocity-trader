package com.univocity.trader.strategy;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

public interface Engine {

	TradingManager getTradingManager() ;

	String getSymbol() ;

	void process(Candle candle, boolean initializing);
}
