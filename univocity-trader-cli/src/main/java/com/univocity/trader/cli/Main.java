package com.univocity.trader.cli;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.binance.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.tickers.Ticker.*;
import com.univocity.trader.tickers.*;
import org.apache.commons.cli.*;

import java.util.*;

public class Main {
	/**
	 * configfile option
	 */
	private static final String CONFIG_OPTION = "config";
	/**
	 * account option
	 */
	private static final String ACCOUNT_OPTION = "account";
	/**
	 * import option
	 */
	private static final String IMPORT_OPTION = "import";
	/**
	 * simulate option
	 */
	private static final String SIMULATE_OPTION = "simulate";
	/**
	 * livetrade option
	 */
	private static final String LIVETRADE_OPTION = "livetrade";

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

	private static void livetrade(Exchange exchange, Account account) {
		// TODO
	}

	public static void main(String... args) {
		System.out.println("Univocity CLI");
		/*
		 * options
		 */
		final Options options = new Options();
		Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(false).desc("config file").build();
		options.addOption(oo);
		oo = Option.builder().argName(ACCOUNT_OPTION).longOpt(ACCOUNT_OPTION).type(String.class).hasArg().required(false).desc("account").build();
		options.addOption(oo);
		oo = Option.builder().argName(IMPORT_OPTION).longOpt(IMPORT_OPTION).hasArg(false).required(false).desc("import").build();
		options.addOption(oo);
		oo = Option.builder().argName(SIMULATE_OPTION).longOpt(SIMULATE_OPTION).hasArg(false).required(false).desc("simulate").build();
		options.addOption(oo);
		oo = Option.builder().argName(LIVETRADE_OPTION).longOpt(LIVETRADE_OPTION).hasArg(false).required(false).desc("live trade").build();
		options.addOption(oo);
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
//			final String configFileName = cmd.getOptionValue(CONFIG_OPTION);
//			if (null != configFileName) {
//				Configuration.load(configFileName);
//			} else {
//				Configuration.load();
//			}
			/*
			 * TODO we might want to fix this....
			 */
			final Exchange exchange = new BinanceExchange();
			final String accountName = cmd.getOptionValue(ACCOUNT_OPTION);
			Account account = null;
			if (null != accountName) {
//				account = Binance.load(CONFIG_OPTION).account(accountName);
			}
			/*
			 * run command
			 */
			if (cmd.hasOption(IMPORT_OPTION)) {
				/*
				 * update market history
				 */
				updateMarketHistory();
			} else if (cmd.hasOption(SIMULATE_OPTION)) {
				/*
				 * simulate
				 */
				simulate(exchange, account);
			} else if (cmd.hasOption(LIVETRADE_OPTION)) {
				/*
				 * live trade
				 */
				livetrade(exchange, account);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("posix", options);
		}
	}

	private static void simulate(Exchange exchange, Account account) {
		final SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
		account.listeners().add(stats);
		final Binance.Simulator simulation = Binance.simulator();
		simulation.run();
		stats.printTradeStats();
	}

	private static void updateMarketHistory() {
		Binance.simulator().updateHistory();
	}
}