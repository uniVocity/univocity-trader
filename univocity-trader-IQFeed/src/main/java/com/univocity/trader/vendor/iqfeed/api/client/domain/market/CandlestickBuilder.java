package com.univocity.trader.vendor.iqfeed.api.client.domain.market;

public final class CandlestickBuilder {
    protected  Long openTime;
    protected String ID;
    protected String open;
    protected String high;
    protected String low;
    protected String close;
    protected String volume;
    protected String lastSize;
    protected String last;
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
    protected String quoteAssetVolume;
    protected Long numberOfTrades;
    protected String takerBuyBaseAssetVolume;
    protected String takerBuyQuoteAssetVolume;

    public CandlestickBuilder() {
    }

    public static CandlestickBuilder aCandlestick() {
        return new CandlestickBuilder();
    }

    public CandlestickBuilder setOpenTime(Long openTime) {
        this.openTime = openTime;
        return this;
    }

    public CandlestickBuilder setID(String ID) {
        this.ID = ID;
        return this;
    }

    public CandlestickBuilder setOpen(String open) {
        this.open = open;
        return this;
    }

    public CandlestickBuilder setHigh(String high) {
        this.high = high;
        return this;
    }

    public CandlestickBuilder setLow(String low) {
        this.low = low;
        return this;
    }

    public CandlestickBuilder setClose(String close) {
        this.close = close;
        return this;
    }

    public CandlestickBuilder setVolume(String volume) {
        this.volume = volume;
        return this;
    }

     public CandlestickBuilder setLast(String last) {
        this.last = last;
        return this;
    }

    public CandlestickBuilder setLastSize(String lastSize) {
        this.lastSize = lastSize;
        return this;
    }

    public CandlestickBuilder setTotalVolume(String totalVolume) {
        this.totalVolume = totalVolume;
        return this;
    }

    public CandlestickBuilder setPeriodVolume(String periodVolume) {
        this.periodVolume = periodVolume;
        return this;
    }

    public CandlestickBuilder setNumTrades(String numTrades) {
        this.numTrades = numTrades;
        return this;
    }

    public CandlestickBuilder setBid(String bid) {
        this.bid = bid;
        return this;
    }

    public CandlestickBuilder setAsk(String ask) {
        this.ask = ask;
        return this;
    }

    public CandlestickBuilder setBasis(String basis) {
        this.basis = basis;
        return this;
    }

    public CandlestickBuilder setMarketCenter(String marketCenter) {
        this.marketCenter = marketCenter;
        return this;
    }

    public CandlestickBuilder setConditions(String conditions) {
        this.conditions = conditions;
        return this;
    }

    public CandlestickBuilder setAggressor(String aggressor) {
        this.aggressor = aggressor;
        return this;
    }

    public CandlestickBuilder setDayCode(String dayCode) {
        this.dayCode = dayCode;
        return this;
    }

    public CandlestickBuilder setCloseTime(Long closeTime) {
        this.closeTime = closeTime;
        return this;
    }

    public CandlestickBuilder setQuoteAssetVolume(String quoteAssetVolume) {
        this.quoteAssetVolume = quoteAssetVolume;
        return this;
    }

    public CandlestickBuilder setNumberOfTrades(Long numberOfTrades) {
        this.numberOfTrades = numberOfTrades;
        return this;
    }

    public CandlestickBuilder setTakerBuyBaseAssetVolume(String takerBuyBaseAssetVolume) {
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
        return this;
    }

    public CandlestickBuilder setTakerBuyQuoteAssetVolume(String takerBuyQuoteAssetVolume) {
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
        return this;
    }

    public CandlestickBuilder setOpenInterest(String openInterest){
        this.openInterest = openInterest;
        return this;
    }

    public Candlestick build() {
        Candlestick candlestick = new Candlestick();
        candlestick.setOpenTime(openTime);
        candlestick.setID(ID);
        candlestick.setOpen(open);
        candlestick.setHigh(high);
        candlestick.setLow(low);
        candlestick.setClose(close);
        candlestick.setVolume(volume);
        candlestick.setLast(last);
        candlestick.setLastSize(lastSize);
        candlestick.setTotalVolume(totalVolume);
        candlestick.setPeriodVolume(periodVolume);
        candlestick.setNumTrades(numTrades);
        candlestick.setOpenInterest(openInterest);
        candlestick.setBid(bid);
        candlestick.setAsk(ask);
        candlestick.setBasis(basis);
        candlestick.setMarketCenter(marketCenter);
        candlestick.setConditions(conditions);
        candlestick.setAggressor(aggressor);
        candlestick.setDayCode(dayCode);
        candlestick.setCloseTime(closeTime);
        candlestick.setQuoteAssetVolume(quoteAssetVolume);
        candlestick.setNumberOfTrades(numberOfTrades);
        candlestick.setTakerBuyBaseAssetVolume(takerBuyBaseAssetVolume);
        candlestick.setTakerBuyQuoteAssetVolume(takerBuyQuoteAssetVolume);
        return candlestick;
    }
}
