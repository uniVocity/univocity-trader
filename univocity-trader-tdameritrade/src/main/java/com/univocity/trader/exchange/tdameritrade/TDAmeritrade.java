package com.univocity.trader.exchange.tdameritrade;

//import com.univocity.trader.EntryPoint;
//import com.univocity.trader.Exchange;
//import com.univocity.trader.account.Trade;
//import com.univocity.trader.candles.Candle;
//import com.univocity.trader.simulation.MarketSimulator;
//
//import java.io.ObjectInputFilter;
//import java.util.Collection;
//
//public class TDAmeritrade implements EntryPoint {
//    private static final class Configuration extends com.univocity.trader.config.Configuration<Configuration, Account>{
//        private Configuration() { super("ib.properties");}
//
//        @Override
//        protected Account newAccountConfiguration(String id){ return new Account(id);}
//    }
//
//    public static final class Simulator extends MarketSimulator<Configuration, Account>{
//        private Simulator() { super(new Configuration(), TDA::new);}
//        @Override
//        protected void backfillHistory(Exchange<?, Account> exchange, Collection<String> symbols){
//            for(Account account: configure().accounts()){
//                exchange.connectToAccount(account);
//            }
//            super.backfillHistory(exchange, symbols);
//        }
//    }
//
//    public static final class Trader extends LiveTrader<Candle, Configuration, Account>{
//        private Trader(){
//            super(new TDA(), new Configuration()
//            .updateHistoryBeforeLiveTrading(false)
//            .pollCandles(false)
//            );
//        }
//    }
//
//    public static Simulator simulator(){ return new Simulator();}
//
//    public static Trader trader(){
//        return new Trader();
//    }
//}
