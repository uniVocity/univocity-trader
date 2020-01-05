package com.univocity.trader.vendor.iqfeed.api.client.domain.candles;

// TODO: extend standard Candle class?
public class IQFeedCandle {

	public IQFeedCandle(Double open, Double high, Double low, Double close, Double volume, Long opentime, Long closeTime,
						Double openInterest, Double last, Double lastSize, Double periodVolume, Double totalVolume, Double numTrades, Double bid,
						Double ask, String basis, String marketCenter, String conditions, String aggressor, String dayCode, String ID) {

		this.openTime = opentime;
		this.closeTime = closeTime;
		this.openInterest = openInterest;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.last = last;
		this.lastSize = lastSize;
		this.periodVolume = periodVolume;
		this.totalVolume = totalVolume;
		this.numTrades = numTrades;
		this.bid = bid;
		this.ask = ask;
		this.basis = basis;
		this.marketCenter = marketCenter;
		this.conditions = conditions;
		this.aggressor = aggressor;
		this.dayCode = dayCode;
		this.ID = ID;
	}

	Double open;
	Double high;
	Double low;
	Double close;
	Double volume;

	Long openTime;
	Long closeTime;
	Double last;
	Double lastSize;
	Double periodVolume;
	Double totalVolume;
	Double numTrades;
	Double bid;
	Double ask;
	Double openInterest;
	String basis;
	String marketCenter;
	String conditions;
	String aggressor;
	String dayCode;
	String ID;

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public Double getOpenInterest() {
		return openInterest;
	}

	public void setOpenInterest(Double openInterest) {
		this.openInterest = openInterest;
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public Long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(Long openTime) {
		this.openTime = openTime;
	}

	public Long getCloseTime() {
		return openTime;
	}

	public void setCloseTime(Long closeTime) {
		this.openTime = openTime;
	}

	public Double getLast() {
		return last;
	}

	public void setLast(Double last) {
		this.last = last;
	}

	public Double getLastSize() {
		return lastSize;
	}

	public void setLastSize(Double lastSize) {
		this.lastSize = lastSize;
	}

	public Double getPeriodVolume() {
		return periodVolume;
	}

	public void setPeriodVolume(Double periodVolume) {
		this.periodVolume = periodVolume;
	}

	public Double getTotalVolume() {
		return totalVolume;
	}

	public void setTotalVolume(Double totalVolume) {
		this.totalVolume = totalVolume;
	}

	public Double getNumTrades() {
		return numTrades;
	}

	public void setNumTrades(Double numTrades) {
		this.numTrades = numTrades;
	}

	public Double getBid() {
		return bid;
	}

	public void setBid(Double bid) {
		this.bid = bid;
	}

	public Double getAsk() {
		return ask;
	}

	public void setAsk(Double ask) {
		this.ask = ask;
	}

	public String getBasis() {
		return basis;
	}

	public void setBasis(String basis) {
		this.basis = basis;
	}

	public String getMarketCenter() {
		return marketCenter;
	}

	public void setMarketCenter(String marketCenter) {
		this.marketCenter = marketCenter;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}

	public String getAggressor() {
		return aggressor;
	}

	public void setAggressor(String aggressor) {
		this.aggressor = aggressor;
	}

	public String getDayCode() {
		return dayCode;
	}

	public void setDayCode(String dayCode) {
		this.dayCode = dayCode;
	}

	public static final class IQFeedCandleBuilder {
		Double open;
		Double high;
		Double low;
		Double close;
		Double volume;
		Long openTime;
		Long closeTime;
		Double last;
		Double lastSize;
		Double periodVolume;
		Double totalVolume;
		Double numTrades;
		Double bid;
		Double ask;
		Double openInterest;
		String basis;
		String marketCenter;
		String conditions;
		String aggressor;
		String dayCode;
		String ID;

		public IQFeedCandleBuilder() {
		}

		public static IQFeedCandleBuilder anIQFeedCandle() {
			return new IQFeedCandleBuilder();
		}

		public IQFeedCandleBuilder setOpen(Double open) {
			this.open = open;
			return this;
		}

		public IQFeedCandleBuilder setHigh(Double high) {
			this.high = high;
			return this;
		}

		public IQFeedCandleBuilder setLow(Double low) {
			this.low = low;
			return this;
		}

		public IQFeedCandleBuilder setClose(Double close) {
			this.close = close;
			return this;
		}

		public IQFeedCandleBuilder setVolume(Double volume) {
			this.volume = volume;
			return this;
		}

		public IQFeedCandleBuilder setOpenTime(Long openTime) {
			this.openTime = openTime;
			return this;
		}

		public IQFeedCandleBuilder setCloseTime(Long closeTime) {
			this.closeTime = closeTime;
			return this;
		}

		public IQFeedCandleBuilder setLast(Double last) {
			this.last = last;
			return this;
		}

		public IQFeedCandleBuilder setLastSize(Double lastSize) {
			this.lastSize = lastSize;
			return this;
		}

		public IQFeedCandleBuilder setPeriodVolume(Double periodVolume) {
			this.periodVolume = periodVolume;
			return this;
		}

		public IQFeedCandleBuilder setTotalVolume(Double totalVolume) {
			this.totalVolume = totalVolume;
			return this;
		}

		public IQFeedCandleBuilder setNumTrades(Double numTrades) {
			this.numTrades = numTrades;
			return this;
		}

		public IQFeedCandleBuilder setBid(Double bid) {
			this.bid = bid;
			return this;
		}

		public IQFeedCandleBuilder setAsk(Double ask) {
			this.ask = ask;
			return this;
		}

		public IQFeedCandleBuilder setOpenInterest(Double openInterest) {
			this.openInterest = openInterest;
			return this;
		}

		public IQFeedCandleBuilder setBasis(String basis) {
			this.basis = basis;
			return this;
		}

		public IQFeedCandleBuilder setMarketCenter(String marketCenter) {
			this.marketCenter = marketCenter;
			return this;
		}

		public IQFeedCandleBuilder setConditions(String conditions) {
			this.conditions = conditions;
			return this;
		}

		public IQFeedCandleBuilder setAggressor(String aggressor) {
			this.aggressor = aggressor;
			return this;
		}

		public IQFeedCandleBuilder setDayCode(String dayCode) {
			this.dayCode = dayCode;
			return this;
		}

		public IQFeedCandleBuilder setID(String ID) {
			this.ID = ID;
			return this;
		}

		public IQFeedCandle build() {
			IQFeedCandle iQFeedCandle = new IQFeedCandle(open, high, low, close, volume, openTime, closeTime, openInterest, last, lastSize, periodVolume, totalVolume, numTrades, bid, ask, basis, marketCenter, conditions, aggressor, dayCode, ID);
			iQFeedCandle.setOpenTime(openTime);
			return iQFeedCandle;
		}
	}
}

