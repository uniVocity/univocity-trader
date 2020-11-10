package com.univocity.trader.exchange.tdameritrade;

//import com.univocity.trader.Exchange;
//import com.univocity.trader.candles.Candle;
//import com.univocity.trader.candles.PreciseCandle;
//import com.univocity.trader.candles.SymbolInformation;
//import com.univocity.trader.candles.TickConsumer;
//import com.univocity.trader.exchange.tdameritrade.api.TDAIncomingCandles;
//import com.univocity.trader.exchange.tdameritrade.api.TDAmeritradeApi;
//import com.univocity.trader.indicators.base.TimeInterval;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Collections;
//import java.util.Map;
//
//class TDA implements Exchange<Candle, Account> {
//    Logger log = LoggerFactory.getLogger(this.getClass());
//
//
//    public TDA(String ip, int port, int clientID, String optionalCapabilities) {
//        api = new TDAmeritradeApi(ip, port, clientID, optionalCapabilities, this::reconnectApi);
//    }
//
//    public void reconnectApi(){}
//
//    @Override
//    public TDAAccount connectToAccount(Account account){
//        TDAAccount tdAccount = new TDAAccount();
//    }
//
//    @Override
//    public TDAIncomingCandles getLatestTicks(String symbol, TimeInterval interval){
//        return new TDAIncomingCandles();
//    }
//
//    @Override
//    public TDAIncomingCandles getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime){
//        return new TDAIncomingCandles();
//    }
//
//    @Override
//    public PreciseCandle generatePreciseCandle(Candle exchangeCandle){
//        return new PreciseCandle();
//    }
//
//    @Override
//    public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candle> consumer){
//    }
//
//    @Override
//    public void closeLiveStream() throws Exception {
//    }
//
//    public Candle getLatestTick(String symbol, TimeInterval interval){
//
//    }
//    @Override
//    public Map<String, double[]> getLatestPrices(){
//        return Collections.emptyMap();
//    }
//
//    @Override
//    public Map<String, SymbolInformation> getSymbolInformation(){
//        return Collections.emptyMap();
//    }
//
//    @Override
//    public double getLatestPrice(String assetSymbol, String fundSymbol){
//        return 0;
//    }
//
//    @Override
//    public int historicalCandleCountLimit(){
//        return 0;
//    }
//
//    @Override
//    public long timeToWaitPerRequest(){
//        return 0;
//    }
//
//    public synchronized void getAccountBalances(){
//        if(accountBalanceRequest == 0){
//            accountBalanceRequest = this.api.loadAccountPositions((balance -> out.put(balance.getSymbol(), balance)));
//            api.waitForResponse(accountBalanceReqeust, 5);
//        }
//
//    }
//
//}
