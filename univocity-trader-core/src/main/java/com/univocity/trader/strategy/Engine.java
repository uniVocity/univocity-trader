package com.univocity.trader.strategy;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import java.util.*;

import static com.univocity.trader.utils.NewInstances.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Engine {

	private static final Logger log = LoggerFactory.getLogger(Engine.class);

	private final Trader trader;
	private final Strategy[] strategies;
	private final Strategy[] plainStrategies;
	private final IndicatorGroup[] indicatorGroups;

	private final TradingManager tradingManager;
	private final Aggregator[] aggregators;

	public Engine(TradingManager tradingManager, InstancesProvider<Strategy> strategies, InstancesProvider<StrategyMonitor> monitors, Set<Object> allInstances) {
		this(tradingManager, strategies, monitors, Parameters.NULL, allInstances);
	}

	public Engine(TradingManager tradingManager, InstancesProvider<Strategy> strategies, InstancesProvider<StrategyMonitor> monitors, Parameters parameters, Set<Object> allInstances) {
		this.tradingManager = tradingManager;
		this.trader = new Trader(tradingManager, monitors, parameters, allInstances);
		this.strategies = getInstances(tradingManager.getSymbol(), parameters, strategies, "Strategy", true, allInstances);

		Set<IndicatorGroup> groups = new LinkedHashSet<>();
		Set<Strategy> plainStrategies = new LinkedHashSet<>();
		for (Strategy strategy : this.strategies) {
			if (strategy instanceof IndicatorGroup) {
				groups.add((IndicatorGroup) strategy);
			} else {
				plainStrategies.add(strategy);
			}
		}
		Collections.addAll(groups, trader.getStrategyMonitors());
		indicatorGroups = groups.toArray(new IndicatorGroup[0]);

		Aggregator rootAggregator = new Aggregator(trader.getSymbol() + parameters.toString());
		for (int i = 0; i < indicatorGroups.length; i++) {
			indicatorGroups[i].initialize(rootAggregator);
		}
		aggregators = rootAggregator.getAggregators();

		this.plainStrategies = plainStrategies.toArray(new Strategy[0]);
	}

	public void process(Candle candle, boolean initializing) {
		for (int i = 0; i < aggregators.length; i++)
			aggregators[i].aggregate(candle);

		for (int i = 0; i < indicatorGroups.length; i++) {
			indicatorGroups[i].accumulate(candle);
		}

		if (initializing) { //ignore any signals and just all strategies to populate their internal state
			for (int i = 0; i < plainStrategies.length; i++) {
				plainStrategies[i].getSignal(candle);
			}
			return;
		}

		//for simulations only - tries to fill open orders using the latest candle information loaded from history.
		tradingManager.updateOpenOrders(trader.getSymbol(), candle);


		for (int i = 0; i < strategies.length; i++) {
			Strategy strategy = strategies[i];
			Signal signal = strategy.getSignal(candle);
			try {
				trader.trade(candle, signal, strategy);
			} catch (Exception e) {
				log.error("Error processing " + signal + " generated using candle (" + candle + ") from " + strategy, e);
			}
		}
	}

	public TradingManager getTradingManager() {
		return tradingManager;
	}

	public String getSymbol() {
		return tradingManager.getSymbol();
	}

}
