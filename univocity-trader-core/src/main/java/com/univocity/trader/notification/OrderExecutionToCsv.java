package com.univocity.trader.notification;

import com.univocity.parsers.csv.*;
import com.univocity.trader.account.*;
import org.slf4j.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class OrderExecutionToCsv implements OrderListener {

	private static final Logger log = LoggerFactory.getLogger(OrderExecutionToCsv.class);

	private final File outputDir;
	private final String fileName;

	private List<OrderExecutionLine> lines = new ArrayList<>();

	public OrderExecutionToCsv() {
		this(new File(System.getProperty("user.home") + "/Documents"), "market_simulation");
	}

	public OrderExecutionToCsv(String fileName) {
		this(new File(System.getProperty("user.home") + "/Documents"), fileName);
	}

	public OrderExecutionToCsv(File outputDir, String fileName) {
		this.outputDir = outputDir;
		this.fileName = fileName;
	}

	@Override
	public void orderSubmitted(Order order, Trade trade, Client client) {
		logDetails(order, trade, client);
	}

	@Override
	public void orderFinalized(Order order, Trade trade, Client client) {
		logDetails(order, trade, client);
	}


	private void logDetails(Order order, Trade trade, Client client) {
		lines.add(new OrderExecutionLine(order, trade, client));
	}

	@Override
	public void simulationEnded(Trader trader, Client client) {
		File out = new File(outputDir.getAbsolutePath() + "/" + fileName + "_" + System.currentTimeMillis() + ".csv");

		CsvRoutines routines = new CsvRoutines(Csv.writeExcel());
		routines.getWriterSettings().setHeaderWritingEnabled(true);

		String[] headers = new String[]{
				"closeTime", "clientId", "operation",
				"quantity", "symbol", "price", "currency", "orderAmount",
				"orderType", "status", "duration",

				"orderFillPercentage", "executedQuantity", "valueTransacted",

				"profitLoss", "profitLossPct", "referenceCurrency", "profitLossReferenceCurrency", "holdings", "freeBalance", "freeBalanceReferenceCurrency",

				"priceChangePct", "minPrice", "minChangePct", "maxChangePct", "maxPrice",

				"estimatedProfitLossPct", "exitReason", "ticks",

				"shortedQuantity", "marginReserve",
		};

		routines.writeAll(lines, OrderExecutionLine.class, out, Charset.forName("windows-1252"), headers);
		log.info("Written simulation statistics to {}", out.getAbsolutePath());
	}
}
