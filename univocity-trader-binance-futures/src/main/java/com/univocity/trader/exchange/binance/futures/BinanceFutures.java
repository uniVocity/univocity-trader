package com.univocity.trader.exchange.binance.futures;

import com.univocity.trader.EntryPoint;
import com.univocity.trader.LiveTrader;
import com.univocity.trader.exchange.binance.futures.model.market.Candlestick;
import com.univocity.trader.simulation.MarketSimulator;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class BinanceFutures implements EntryPoint {

    public static final class Configuration extends com.univocity.trader.config.Configuration<Configuration, Account> {
        private Configuration() {
            super("binance.properties");
        }

        @Override
        protected Account newAccountConfiguration(String id) {
            return new Account(id);
        }
    }

    public static final class Simulator extends MarketSimulator<Configuration, Account> {
        private Simulator() {
            super(new Configuration(), BinanceFuturesExchange::new);
        }
    }

    public static final class Trader extends LiveTrader<Candlestick, Configuration, Account> {
        private Trader() {
            super(new BinanceFuturesExchange(), new Configuration());
        }
    }

    public static Simulator simulator() {
        return new Simulator();
    }

    public static Trader trader() {
        return new Trader();
    }
}
