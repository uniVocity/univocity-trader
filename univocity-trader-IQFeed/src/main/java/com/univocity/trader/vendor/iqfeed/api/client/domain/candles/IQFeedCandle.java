package com.univocity.trader.vendor.iqfeed.api.client.domain.candles;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.vendor.iqfeed.api.client.constant.IQFeedApiConstants;

public class IQFeedCandle extends Candle {

    public IQFeedCandle(Long dateTime, Double last, Double lastSize, Double periodVolume, Double totalVolume,
                        Double numTrades, Double bid, Double ask, String basis, String marketCenter, String conditions,
                        String aggressor, String dayCode, String ID) {
        this.dateTime = dateTime;
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

    Long dateTime;
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

    public Long getDateTime() {
        return dateTime;
    }

    public void setDateTime(Long dateTime) {
        this.dateTime = dateTime;
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
        public long openTime;
        public long closeTime;
        public double open;
        public double high;
        public double low;
        public double close;
        public double volume;
        public boolean merged;
        Long dateTime;
        Double last;
        Double lastSize;
        Double periodVolume;
        Double totalVolume;
        Double numTrades;
        Double bid;
        Double ask;
        String basis;
        String marketCenter;
        String conditions;
        String aggressor;
        String dayCode;
        String ID;
        Double openInterest;

        public IQFeedCandleBuilder() {
        }

        public static IQFeedCandleBuilder anIQFeedCandle() {
            return new IQFeedCandleBuilder();
        }

        public IQFeedCandleBuilder setOpenTime(long openTime) {
            this.openTime = openTime;
            return this;
        }

        public IQFeedCandleBuilder setOpenInterest(Double openInterest){
            this.openInterest = openInterest;
            return this;
        }

        public IQFeedCandleBuilder setCloseTime(long closeTime) {
            this.closeTime = closeTime;
            return this;
        }

        public IQFeedCandleBuilder setOpen(double open) {
            this.open = open;
            return this;
        }

        public IQFeedCandleBuilder setHigh(double high) {
            this.high = high;
            return this;
        }

        public IQFeedCandleBuilder setLow(double low) {
            this.low = low;
            return this;
        }

        public IQFeedCandleBuilder setClose(double close) {
            this.close = close;
            return this;
        }

        public IQFeedCandleBuilder setVolume(double volume) {
            this.volume = volume;
            return this;
        }

        public IQFeedCandleBuilder setDateTime(Long dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public IQFeedCandleBuilder setMerged(boolean merged) {
            this.merged = merged;
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
            IQFeedCandle iQFeedCandle = new IQFeedCandle(dateTime, last, lastSize, periodVolume, totalVolume, numTrades, bid, ask, basis, marketCenter, conditions, aggressor, dayCode, ID);
            return iQFeedCandle;
        }
    }
}

