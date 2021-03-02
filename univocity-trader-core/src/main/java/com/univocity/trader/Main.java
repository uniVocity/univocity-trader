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
	/**
	 * spot test network option
	 */
	private static final String TEST_NET_OPTION = "testnet";

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
		options.addOption(Option.builder().argName(TEST_NET_OPTION).longOpt(TEST_NET_OPTION).hasArg(false).required(false).desc("trade live on on test network").build());
		/*
		 * parse
		 */
		final CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			boolean ran = false;
			cmd = parser.parse(options, args);
			/*
			 * config
			 */
			final String configFileName = cmd.getOptionValue(CONFIG_OPTION);
			final String exchangeName = cmd.getOptionValue(EXCHANGE_OPTION);
			EntryPoint entryPoint = Utils.findClassAndInstantiate(EntryPoint.class, exchangeName);

			if (cmd.hasOption(TRADE_OPTION)) {
				ran = true;
				final boolean isTestNet = cmd.hasOption(TEST_NET_OPTION);
				livetrade(entryPoint, configFileName, isTestNet);
			} else {
				AbstractSimulator<?, ?> simulator = loadSimulator(entryPoint, configFileName);
				/*
				 * run command
				 */
				if (cmd.hasOption(BACKFILL_OPTION)) {
					ran = true;
					/*
					 * update market history
					 */
					if (simulator instanceof MarketSimulator) {
						((MarketSimulator)simulator).backfillHistory();
					} else {
						throw new IllegalArgumentException(BACKFILL_OPTION + " is not supported by " + exchangeName);
					}
				}
				if (cmd.hasOption(SIMULATE_OPTION)) {
					ran = true;
					/*
					 * simulate
					 */
					simulator.run();
				}
			}
			if (!ran) {
				throw new IllegalArgumentException("Please provide an action to execute: " + BACKFILL_OPTION + ", " + SIMULATE_OPTION + " or " + TRADE_OPTION);
			}
		} catch (final Exception e) {
			System.err.println(e.getMessage());
			final HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(null);
			formatter.printHelp("posix", options);
		} finally {
			if (cmd.hasOption(BACKFILL_OPTION) && !cmd.hasOption(TRADE_OPTION)){
				// exit after backfill as any HTTP client used might be configured to be kept alive and prevent the program from exiting.
				System.exit(0);
			}
		}
	}

	private static void livetrade(EntryPoint entryPoint, String configFileName, boolean isTestNet) {
		var trader = (LiveTrader<?, ?, ?>) ReflectionUtils.invokeMethod(entryPoint, "trader", true);
		Configuration configuration = loadConfiguration(trader.configure(), configFileName);
		configuration.setTestNet(isTestNet);
		trader.run();
	}

	private static AbstractSimulator<?, ?> loadSimulator(EntryPoint entryPoint, String configFileName) {
		var out = (AbstractSimulator<?, ?>) ReflectionUtils.invokeMethod(entryPoint, "simulator", true);
		loadConfiguration(out.configure(), configFileName);
		return out;
	}

	private static Configuration loadConfiguration(Configuration config, String fileName) {
		if (fileName != null) {
			config.loadConfigurationFromProperties(fileName);
		} else {
			config.loadConfigurationFromProperties();
		}
		return config;
	}
}