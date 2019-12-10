package com.univocity.trader.vendor.iqfeed.api.client.domain.market;

import com.univocity.trader.vendor.iqfeed.api.client.constant.IQFeedApiConstants;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonFormat(shape=JsonFormat.Shape.ARRAY)
@JsonPropertyOrder()
@JsonIgnoreProperties(ignoreUnknown = true)
public class Candlestick{
    protected  Long openTime;
    protected String ID;
    protected String open;
    protected String high;
    protected String low;
    protected String close;
    protected String volume;
    protected String last;
    protected String lastSize;
    protected String totalVolume;
    protected String periodVolume;
    protected String openInterest;
    protected String numTrades;
    protected String bid;
    protected String ask;
    protected String basis;
    protected String marketCenter;
    protected String conditions;
    protected String aggressor;
    protected String dayCode;
    protected Long closeTime;

    public Long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Long openTime) {
        this.openTime = openTime;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public Long getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Long closeTime) {
        this.closeTime = closeTime;
    }

    public String getQuoteAssetVolume() {
        return quoteAssetVolume;
    }

    public void setQuoteAssetVolume(String quoteAssetVolume) {
        this.quoteAssetVolume = quoteAssetVolume;
    }

    public Long getNumberOfTrades() {
        return numberOfTrades;
    }

    public void setNumberOfTrades(Long numberOfTrades) {
        this.numberOfTrades = numberOfTrades;
    }

    public String getTakerBuyBaseAssetVolume() {
        return takerBuyBaseAssetVolume;
    }

    public void setTakerBuyBaseAssetVolume(String takerBuyBaseAssetVolume) {
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
    }

    public String getLastSize() {
        return lastSize;
    }

    public void setLastSize(String lastSize) {
        this.lastSize = lastSize;
    }

    public String getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(String totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getPeriodVolume() {
        return periodVolume;
    }

    public void setPeriodVolume(String periodVolume) {
        this.periodVolume = periodVolume;
    }

    public String getNumTrades() {
        return numTrades;
    }

    public void setNumTrades(String numTrades) {
        this.numTrades = numTrades;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
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

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public void setOpenInterest(String openInterest){
        this.openInterest = openInterest;
    }

    public String getTakerBuyQuoteAssetVolume() {
        return takerBuyQuoteAssetVolume;
    }

    public void setTakerBuyQuoteAssetVolume(String takerBuyQuoteAssetVolume) {
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    protected String quoteAssetVolume;
    protected Long numberOfTrades;
    protected String takerBuyBaseAssetVolume;
    protected String takerBuyQuoteAssetVolume;

    @Override
    public String toString() {
        return new ToStringBuilder(this, IQFeedApiConstants.TO_STRING_BUILDER_STYLE)
                .append("openTime", openTime)
                .append("open", open)
                .append("high", high)
                .append("low", low)
                .append("close", close)
                .append("volume", volume)
                .append("closeTime", closeTime)
                .append("quoteAssetVolume", quoteAssetVolume)
                .append("numberOfTrades", numberOfTrades)
                .append("takerBuyBaseAssetVolume", takerBuyBaseAssetVolume)
                .append("takerBuyQuoteAssetVolume", takerBuyQuoteAssetVolume)
                .toString();
    }
}

