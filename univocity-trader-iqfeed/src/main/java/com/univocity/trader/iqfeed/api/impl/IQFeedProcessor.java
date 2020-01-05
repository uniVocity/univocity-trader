package com.univocity.trader.iqfeed.api.impl;

import com.univocity.trader.iqfeed.api.domain.candles.*;
import com.univocity.trader.iqfeed.api.domain.request.*;

import java.time.*;
import java.util.*;

public class IQFeedProcessor {


	public List<IQFeedCandle> candles = new ArrayList<>();
	public Object in;
	public IQFeedHistoricalRequest latestRequest;
	public Long closeOffset;

	public void process(String payload) {
		String dataQualifier = latestRequest.getHeader().substring(1, 2);
		switch (dataQualifier.toUpperCase()) {
			case "I":
				this.candles = processHIX(payload);
			case "T":
				this.candles = processHTX(payload);
			case "M":
			case "D":
			case "W":
				this.candles = processHDX(payload);
		}
	}

	public Long formatDate(String dateString) {
		String date = dateString.replace(" ", "T");
		date = date + "Z";
		return Instant.parse(date).toEpochMilli();
	}

	public List<IQFeedCandle> processHistoricalResponse(String payload) {
		/** Result Format for HTX, HTD, and HTT requests:
		 *  	Request: HTX,GOOG,10<CR><LF>
		 * 2019-04-09 15:05:25.465607,1196.1200,1,513790,1196.1200,1196.3600,971,O,26,113565084,87,0,9,<CR><LF>
		 * 2019-04-09 15:05:24.656735,1196.1200,15,513789,1196.1100,1196.3800,6148,O,5,113565084,3D87,0,9,<CR><LF>
		 * 2019-04-09 15:05:21.268339,1196.1200,1,513774,1196.1100,1196.4300,6147,O,5,113565084,3D87,0,9,<CR><LF>
		 * 2019-04-09 15:05:20.113481,1196.1200,1,513773,1196.1100,1196.4400,970,O,26,113565084,87,0,9,<CR><LF>
		 * 2019-04-09 15:05:20.063681,1196.1200,8,513772,1196.1100,1196.4400,6146,O,5,113565084,87,0,9,<CR><LF>
		 * 2019-04-09 15:05:20.063678,1196.1200,2,513764,1196.1100,1196.4400,6145,O,5,113565084,87,0,9,<CR><LF>
		 * 2019-04-09 15:05:20.059843,1196.1200,98,513762,1196.1100,1196.4400,6144,O,5,113565084,87,0,9,<CR><LF>
		 * 2019-04-09 15:05:20.059839,1196.1500,2,513664,1196.1100,1196.4400,6143,O,5,113565084,87,0,9,<CR><LF>
		 * 2019-04-09 15:05:20.059381,1196.1200,2,513662,1196.1100,1196.4400,969,O,26,113565084,87,0,9,<CR><LF>
		 * 2019-04-09 15:05:20.059218,1196.1500,8,513660,1196.1100,1196.4400,6142,O,5,113565084,3D87,0,9,<CR><LF>
		 * !ENDMSG!,<CR><LF>
		 *
		 * Result Format for HIX, HID, and HIT requests:
		 * Request: HIT,GOOG,60,20130809 155500,20130812 093059,,,,1,TESTREQUEST,2,t<CR><LF>
		 * TESTREQUEST,2013-08-09 15:55:08,890.4600,890.3000,890.3500,890.4000,1220823,8289,60,<CR><LF>
		 * TESTREQUEST,2013-08-09 15:56:06,890.4000,890.1100,890.3900,890.2700,1227889,7066,60,<CR><LF>
		 * TESTREQUEST,2013-08-09 15:57:18,890.3700,890.0400,890.3700,890.2800,1236055,8166,60,<CR><LF>
		 * TESTREQUEST,2013-08-09 15:58:14,890.4000,890.2000,890.2000,890.2200,1244583,8228,60,<CR><LF>
		 * TESTREQUEST,2013-08-09 15:59:05,890.4300,890.2200,890.2200,890.3700,1251876,7293,60,<CR><LF>
		 * TESTREQUEST,2013-08-09 15:59:48,890.4300,890.2000,890.3700,890.2000,1260653,8777,60,<CR><LF>
		 * TESTREQUEST,2013-08-09 18:24:47,892.0663,889.8600,890.2000,890.2300,1323761,63108,56,<CR><LF>
		 * TESTREQUEST,2013-08-12 09:30:11,888.0000,884.0100,884.0100,886.7200,24083,23958,60,<CR><LF>
		 * TESTREQUEST,2013-08-12 09:30:49,886.7980,886.0000,886.7980,886.4600,31219,7136,60,<CR><LF>
		 * TESTREQUEST,!ENDMSG!,<CR><LF>
		 *
		 * Result Format for HDX, HDD, HWT, HMX requests:
		 * Request: HWX,GOOG,10,1,TESTREQUEST,2,0<CR><LF>
		 * TESTREQUEST,2013-05-17,919.98,873.38,878.89,909.18,13029746,0,<CR><LF>
		 * TESTREQUEST,2013-05-24,920.60,871.01,905.00,873.32,11393728,0,<CR><LF>
		 * TESTREQUEST,2013-05-31,892.14,864.29,883.50,871.22,8362804,0,<CR><LF>
		 * TESTREQUEST,2013-06-07,880.00,847.22,873.00,879.73,11707656,0,<CR><LF>
		 * TESTREQUEST,2013-06-14,891.00,865.50,882.10,875.04,10617361,0,<CR><LF>
		 * TESTREQUEST,2013-06-21,910.84,873.07,879.23,880.93,14592220,0,<CR><LF>
		 * TESTREQUEST,2013-06-28,884.69,863.25,871.88,880.37,11683469,0,<CR><LF>
		 * TESTREQUEST,2013-07-05,895.41,877.27,886.45,893.49,6369050,0,<CR><LF>
		 * TESTREQUEST,2013-07-12,923.00,897.08,899.21,923.00,10822255,0,<CR><LF>
		 * TESTREQUEST,2013-07-15,928.00,916.36,924.30,924.69,1961361,0,<CR><LF>
		 * TESTREQUEST,!ENDMSG!,<CR><LF>
		 */


		return null;
	}

	public List<IQFeedCandle> processHTX(String payload) {
		List<IQFeedCandle> candles = new ArrayList<>();
		for (String line : payload.split("\n")) {
			String[] vals = line.split(",");
			String ID = vals[0];
			Long openTime = formatDate(vals[1]);
			Long closeTime = formatDate(vals[1]) + this.latestRequest.getIntervalMillis();
			Double last = Double.valueOf(vals[2]);
			Double lastSize = Double.valueOf(vals[3]);
			Double totalVolume = Double.valueOf(vals[4]);
			Double bid = Double.valueOf(vals[5]);
			Double ask = Double.valueOf(vals[6]);
			String basis = vals[7];
			String marketCenter = vals[8];
			String conditions = vals[9];
			String aggressor = vals[10];
			String daycode = vals[11];
			IQFeedCandle candle = new IQFeedCandle.IQFeedCandleBuilder()
					.setOpenTime(openTime)
					.setCloseTime(closeTime)
					.setLast(last)
					.setLastSize(lastSize)
					.setTotalVolume(totalVolume)
					.setBid(bid)
					.setAsk(ask)
					.setBasis(basis)
					.setMarketCenter(marketCenter)
					.setConditions(conditions)
					.setAggressor(aggressor)
					.setDayCode(daycode)
					.build();
			candles.add(candle);
		}
		return candles;
	}

	public List<IQFeedCandle> processHIX(String payload) {
		List<IQFeedCandle> candles = new ArrayList<>();
		for (String line : payload.split("\n")) {
			String[] vals = line.split(",");
			String ID = vals[0];
			Long openTime = formatDate(vals[1]);
			Long closeTime = openTime + this.latestRequest.getIntervalMillis();
			Double open = Double.valueOf(vals[4]);
			Double high = Double.valueOf(vals[2]);
			Double low = Double.valueOf(vals[3]);
			Double close = Double.valueOf(vals[5]);
			Double totalVolume = Double.valueOf(vals[6]);
			Double periodVolume = Double.valueOf(vals[7]);
			Double numTrades = Double.valueOf(vals[8]);
			IQFeedCandle candle = new IQFeedCandle.IQFeedCandleBuilder()
					.setOpenTime(openTime)
					.setCloseTime(closeTime)
					.setOpen(open)
					.setHigh(high)
					.setLow(low)
					.setClose(close)
					.setVolume(periodVolume)
					.build();
			candles.add(candle);
		}
		return candles;
	}

	public List<IQFeedCandle> processHDX(String payload) {
		List<IQFeedCandle> candles = new ArrayList<>();
		for (String line : payload.split("\n")) {
			String[] vals = line.split(",");
			String ID = vals[0];
			Long openTime = formatDate(vals[1]);
			Long closeTime = openTime + this.latestRequest.getIntervalMillis();
			Double high = Double.valueOf(vals[2]);
			Double low = Double.valueOf(vals[3]);
			Double open = Double.valueOf(vals[4]);
			Double close = Double.valueOf(vals[5]);
			Double periodVolume = Double.valueOf(vals[6]);
			Double openInterest = Double.valueOf(vals[7]);
			IQFeedCandle candle = new IQFeedCandle.IQFeedCandleBuilder()
					.setOpenTime(openTime)
					.setCloseTime(closeTime)
					.setHigh(high)
					.setLow(low)
					.setOpen(open)
					.setClose(close)
					.setVolume(periodVolume)
					.setPeriodVolume(periodVolume)
					.setOpenInterest(openInterest)
					.build();
			candles.add(candle);
		}
		return candles;
	}

	public Object processMarketSummaryResponse(String payload) {
		/**
		 * Request: EDS,11,30,20130110
		 * Symbol,Exchange,Type,Last,TradeSize,TradedMarket,…
		 * AA$Y,34,11,1.64,,34,…..
		 * CBX$Y,34,11,1777.50,0,34,……
		 * CZ$Y,34,11,1.92,,34,,…..
		 * NM$Y,34,11,20.0000,,34,……
		 * NX$Y,34,11,16.7000,1,34,…..
		 * !ENDMSG!,<CR><LF>
		 */
		return null;
	}

	// TODO: add support for these responses
	public Object processNewsResponse(String payload) {
		/**
		 * examples are not properly provided for news response, will need to investigate and implement
		 */
		return null;
	}

	public Object processChainsLookupResponse(String payload) {
		/**
		 *
		 */
		return null;
	}

	public Object processSymbolMarketInfoResponse(String payload) {
		/**
		 *
		 */
		return null;
	}

	public IQFeedHistoricalRequest getLatestRequest() {
		return this.latestRequest;
	}

	public void setLatestRequest(IQFeedHistoricalRequest latestRequest) {
		this.latestRequest = latestRequest;
	}

	public void setCandles(List<IQFeedCandle> candles) {
		this.candles = candles;
	}

	public List<IQFeedCandle> getCandles() {
		return this.candles;
	}


	public Object getIn() {
		return in;
	}

	public void setIn(Object in) {
		this.in = in;
	}

}
