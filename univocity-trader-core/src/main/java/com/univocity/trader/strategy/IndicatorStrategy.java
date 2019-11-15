package com.univocity.trader.strategy;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;

import java.util.*;

public abstract class IndicatorStrategy extends IndicatorGroup implements Strategy {

	protected abstract Set<Indicator> getAllIndicators();

	public abstract Signal getSignal(Candle candle);

}
