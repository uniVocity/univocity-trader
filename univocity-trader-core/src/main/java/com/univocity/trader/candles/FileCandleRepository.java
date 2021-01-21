package com.univocity.trader.candles;

import com.univocity.parsers.common.*;
import org.slf4j.*;

public class FileCandleRepository {

	private static final Logger log = LoggerFactory.getLogger(FileCandleRepository.class);

	private AbstractParser<?> parser;

	private FileCandleRepository(AbstractParser<?> parser){
		this.parser = parser;
	}

}
