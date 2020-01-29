package com.univocity.trader.notification;

import com.univocity.parsers.csv.*;
import com.univocity.trader.account.*;
import org.slf4j.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class OrderExecutionToCsv implements OrderListener {

	private static final Logger log = LoggerFactory.getLogger(OrderExecutionToCsv.class);

	private File outputDir;
	private Supplier<String> fileNameSupplier;
	private boolean omitZeroTrades = true;
	private boolean omitOrderOpening = true;

	private List<OrderExecutionLine> lines = new ArrayList<>();

	public OrderExecutionToCsv() {
		this(new File(System.getProperty("user.home") + "/Documents"), "market_simulation");
	}

	public OrderExecutionToCsv(String fileName) {
		this(new File(System.getProperty("user.home") + "/Documents"), fileName);
	}

	public OrderExecutionToCsv(File outputDir, String fileName) {
		this.outputDir = outputDir;
		this.fileNameSupplier = () -> fileName + "_" + System.currentTimeMillis();
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
		lines.add(new OrderExecutionLine(order, trade, trade.trader(), client));
	}

	private List<OrderExecutionLine> filterLines() {
		Set<String> toRemove = new HashSet<>();
		if (omitZeroTrades) {
			lines.forEach(l -> toRemove.add(l.fillPct == 0.0 && l.status != Order.Status.NEW ? l.orderId : ""));
		}

		return lines.stream()
				.filter(l -> (omitOrderOpening && l.status != Order.Status.NEW))
				.filter(l -> !toRemove.contains(l.orderId))
				.collect(Collectors.toList());
	}

	@Override
	public void simulationEnded(Trader trader, Client client) {
		File out = new File(outputDir.getAbsolutePath() + "/" + fileNameSupplier.get() + ".csv");

		CsvRoutines routines = new CsvRoutines(Csv.writeExcel());
		routines.getWriterSettings().setHeaderWritingEnabled(true);

		String[] headers = new String[]{
				"closeTime", "clientId", "tradeId", "operation",
				"quantity", "assetSymbol", "price", "fundSymbol", "orderAmount",
				"orderType", "status", "duration",
				"orderFillPercentage", "executedQuantity", "valueTransacted",
				"estimatedProfitLossPct", "exitReason", "ticks",
				"profitLossPct", "profitLoss", "freeBalance",
				"priceChangePct", "minPrice", "minChangePct", "maxChangePct", "maxPrice",
				"shortedQuantity", "marginReserve",
				"referenceCurrency", "profitLossReferenceCurrency", "holdings", "freeBalanceReferenceCurrency",
		};

		List<OrderExecutionLine> lines = filterLines();
		if (!lines.isEmpty()) {
			lines.add(new OrderExecutionLine(null, null, trader, client));

			routines.writeAll(lines, OrderExecutionLine.class, out, Charset.forName("windows-1252"), headers);
			log.info("Written simulation statistics to {}", out.getAbsolutePath());

			this.lines.clear();
		}
	}

	public File outputDir() {
		return outputDir;
	}

	public OrderExecutionToCsv outputDir(File outputDir) {
		this.outputDir = outputDir;
		return this;
	}

	public OrderExecutionToCsv fileName(String fileName) {
		return this.fileName(() -> fileName);
	}

	public OrderExecutionToCsv fileName(Supplier<String> fileName) {
		this.fileNameSupplier = fileName;
		return this;
	}

	public boolean omitZeroTrades() {
		return omitZeroTrades;
	}

	public OrderExecutionToCsv omitZeroTrades(boolean omitZeroTrades) {
		this.omitZeroTrades = omitZeroTrades;
		return this;
	}

	public boolean omitOrderOpening() {
		return omitOrderOpening;
	}

	public OrderExecutionToCsv omitOrderOpening(boolean omitOrderOpening) {
		this.omitOrderOpening = omitOrderOpening;
		return this;
	}
}
