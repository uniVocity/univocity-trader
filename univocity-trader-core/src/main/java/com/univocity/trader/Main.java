package com.univocity.trader;

import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.tickers.Ticker.*;
import com.univocity.trader.tickers.*;
import nonapi.io.github.classgraph.utils.*;
import org.apache.commons.cli.*;

import java.util.*;

public class Main {
	/**
	 * configuration file option
	 */
	private static final String CONFIG_OPTION = "config";
	/**
	 * exchange option
	 */
	private static final String EXCHANGE_OPTION = "exchange";
	/**
	 * backfill historical data option
	 */
	private static final String BACKFILL_OPTION = "backfill";
	/**
	 * simulation option
	 */
	private static final String SIMULATE_OPTION = "simulate";
	/**
	 * live trading option
	 */
	private static final String TRADE_OPTION = "trade";

	private static String[] getPairs(Exchange exchange) {
		final String[] univocitySymbols = Tickers.getInstance().getSymbols(Type.crypto);
		final String[] univocityReference = Tickers.getInstance().getSymbols(Type.reference);
		final String[] univocityPairs = Tickers.getInstance().makePairs(univocitySymbols, univocityReference);
		final Map<String, SymbolInformation> symbolInfo = exchange.getSymbolInformation();
		final List<String> lst = new ArrayList<String>();
		for (final String pair : univocityPairs) {
			if (symbolInfo.containsKey(pair)) {
				lst.add(pair);
			}
		}
		final String[] ret = new String[lst.size()];
		lst.toArray(ret);
		return ret;
	}

	public static void main(String... args) {
		/*
		 * options
		 */
		final Options options = new Options();

		options.addOption(Option.builder().argName(EXCHANGE_OPTION).longOpt(EXCHANGE_OPTION).type(String.class).hasArg().required(true).desc("exchange name").build());
		options.addOption(Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(false).desc("configuration file").build());
		options.addOption(Option.builder().argName(BACKFILL_OPTION).longOpt(BACKFILL_OPTION).hasArg(false).required(false).desc("backfill historical data from exchange").build());
		options.addOption(Option.builder().argName(SIMULATE_OPTION).longOpt(SIMULATE_OPTION).hasArg(false).required(false).desc("simulate").build());
		options.addOption(Option.builder().argName(TRADE_OPTION).longOpt(TRADE_OPTION).hasArg(false).required(false).desc("trade live on exchange").build());
		/*
		 * parse
		 */
		final CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			/*
			 * config
			 */
			final String configFileName = cmd.getOptionValue(CONFIG_OPTION);
			final String exchangeName = cmd.getOptionValue(EXCHANGE_OPTION);
			EntryPoint entryPoint = Utils.findClassAndInstantiate(EntryPoint.class, exchangeName);

			if (cmd.hasOption(TRADE_OPTION)) {
				livetrade(entryPoint, configFileName);
			} else {
				AbstractMarketSimulator<?, ?> simulator = loadSimulator(entryPoint, configFileName);
				/*
				 * run command
				 */
				if (cmd.hasOption(BACKFILL_OPTION)) {
					/*
					 * update market history
					 */
					simulator.updateHistory();
				}
				if (cmd.hasOption(SIMULATE_OPTION)) {
					/*
					 * simulate
					 */
					simulator.run();
				}
			}
		} catch (final Exception e) {
			System.err.println(e.getMessage());
			final HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(null);
			formatter.printHelp("posix", options);
		}
	}

	private static void livetrade(EntryPoint entryPoint, String configFileName) {
		var trader = (LiveTrader<?, ?, ?>) ReflectionUtils.invokeMethod(entryPoint, "trader", true);
		loadConfiguration(trader.configure(), configFileName);
		trader.run();
	}

	private static AbstractMarketSimulator<?, ?> loadSimulator(EntryPoint entryPoint, String configFileName) {
		var out = (AbstractMarketSimulator<?, ?>) ReflectionUtils.invokeMethod(entryPoint, "simulator", true);
		loadConfiguration(out.configure(), configFileName);
		return out;
	}

	private static void loadConfiguration(Configuration config, String fileName) {
		if (fileName != null) {
			config.loadConfigurationFromProperties(fileName);
		} else {
			config.loadConfigurationFromProperties();
		}
	}
}