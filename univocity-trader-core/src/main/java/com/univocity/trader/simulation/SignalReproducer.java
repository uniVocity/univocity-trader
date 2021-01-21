package com.univocity.trader.simulation;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

import java.io.*;

public class SignalReproducer implements Strategy {
	private static final Logger log = LoggerFactory.getLogger(SignalReproducer.class);

	private final SignalRepository signalRepository;

	public SignalReproducer(String symbol, Reader input) {
		this(new SignalRepository(symbol, input));
	}

	public SignalReproducer(SignalRepository repository) {
		this.signalRepository = repository;
	}

	@Override
	public Signal getSignal(Candle candle, Context context) {
		return signalRepository.signalFor(context.symbol(), candle);
	}
}
