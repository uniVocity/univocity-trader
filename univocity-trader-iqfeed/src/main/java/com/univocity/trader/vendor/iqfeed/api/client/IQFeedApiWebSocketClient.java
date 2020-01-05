package com.univocity.trader.vendor.iqfeed.api.client;

import com.univocity.trader.vendor.iqfeed.api.client.domain.candles.*;
import com.univocity.trader.vendor.iqfeed.api.client.domain.request.*;

import java.io.*;
import java.util.*;

public interface IQFeedApiWebSocketClient extends Closeable {

	// todo - add more methods for IQFeed

	List<IQFeedCandle> getCandlestickBars(IQFeedHistoricalRequest request);

	List<IQFeedCandle> getHistoricalCandlestickBars(IQFeedHistoricalRequest request);

}
