package com.univocity.trader.strategy;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;

public interface Strategy {

	Signal getSignal(Candle candle);

}
