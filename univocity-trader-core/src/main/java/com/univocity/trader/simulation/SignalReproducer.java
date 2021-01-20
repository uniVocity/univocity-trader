package com.univocity.trader.simulation;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

import java.util.*;

public class SignalReproducer implements Strategy {
	private static final Logger log = LoggerFactory.getLogger(SignalReproducer.class);

	private final Map<Candle, Signal> signals;

	public SignalReproducer(String symbol, SignalRepository repository) {
		signals = repository.signalsFor(symbol);
		if (signals == null) {
			throw new IllegalArgumentException("No signals for " + symbol + " in repository");
		}
	}

	@Override
	public Signal getSignal(Candle candle) {
		Signal out = signals.get(candle);
		if (out != null) {
			return out;
		}
		return Signal.NEUTRAL;
	}
}
