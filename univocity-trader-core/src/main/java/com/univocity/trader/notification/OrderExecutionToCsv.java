package com.univocity.trader.notification;

import com.univocity.parsers.csv.*;
import com.univocity.trader.account.*;
import org.slf4j.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;

public class OrderExecutionToCsv implements OrderListener {

	private static final Logger log = LoggerFactory.getLogger(OrderExecutionToCsv.class);

	private final File outputDir;
	private final String fileName;
	private boolean omitZeroTrades = true;
	private boolean omitNew = true;

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

	private List<OrderExecutionLine> filterLines() {
		Set<String> toRemove = new HashSet<>();
		if (omitZeroTrades) {
			lines.forEach(l -> toRemove.add(l.fillPct == 0.0 && l.status != Order.Status.NEW ? l.orderId : ""));
		}

		return lines.stream()
				.filter(l -> (omitNew && l.status != Order.Status.NEW))
				.filter(l -> !toRemove.contains(l.orderId))
				.collect(Collectors.toList());
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
				"estimatedProfitLossPct", "exitReason", "ticks",
				"profitLossPct", "profitLoss", "freeBalance",
				"priceChangePct", "minPrice", "minChangePct", "maxChangePct", "maxPrice",
				"shortedQuantity", "marginReserve",
				"referenceCurrency", "profitLossReferenceCurrency", "holdings", "freeBalanceReferenceCurrency",
		};

		routines.writeAll(filterLines(), OrderExecutionLine.class, out, Charset.forName("windows-1252"), headers);
		log.info("Written simulation statistics to {}", out.getAbsolutePath());
	}
}
