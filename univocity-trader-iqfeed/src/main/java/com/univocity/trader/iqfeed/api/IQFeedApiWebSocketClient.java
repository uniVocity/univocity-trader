package com.univocity.trader.iqfeed.api;

import com.univocity.trader.iqfeed.api.domain.candles.*;
import com.univocity.trader.iqfeed.api.domain.request.*;

import java.io.*;
import java.util.*;

public interface IQFeedApiWebSocketClient extends Closeable {

	// todo - add more methods for IQFeed

	List<IQFeedCandle> getCandlestickBars(IQFeedHistoricalRequest request);

	List<IQFeedCandle> getHistoricalCandlestickBars(IQFeedHistoricalRequest request);

}
