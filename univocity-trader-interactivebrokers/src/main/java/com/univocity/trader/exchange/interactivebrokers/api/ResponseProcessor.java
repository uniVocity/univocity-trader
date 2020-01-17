package com.univocity.trader.exchange.interactivebrokers.api;

import com.ib.client.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.interactivebrokers.model.account.*;
import com.univocity.trader.exchange.interactivebrokers.model.book.*;
import org.slf4j.*;

import java.util.*;

/**
 * {@link EWrapper} implementation of methods that are being currently used or have some logic in them
 * that is not simply logging.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ResponseProcessor extends IgnoredResponseProcessor {

	private static final Logger log = LoggerFactory.getLogger(ResponseProcessor.class);

	private final RequestHandler requestHandler;
	private AccountBalance accountBalance = new AccountBalance();

	private Map<Integer, TradingBook> marketBooks = new HashMap<>();
	private Map<Integer, TradingBook> smartBooks = new HashMap<>();

	private boolean disconnecting = false;

	public ResponseProcessor(RequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	@Override
	public final void connectAck() {
		log.info("Connected");
	}

	private static Candle translateHistoricalTick(HistoricalTick tick) {
		return new Candle(tick.time() * 1000L, tick.time() * 1000L, tick.price(), tick.price(), tick.price(), tick.price(), tick.size());
	}

	private static Candle translateHistoricalTickLast(HistoricalTickLast tick) {
		return new Candle(tick.time(), tick.time(), tick.price(), tick.price(), tick.price(), tick.price(), tick.size());
	}

	private static Candle translateHistoricalTickBidAsk(HistoricalTickBidAsk tick) {
//		TODO: no abstraction available for this type of candle
//		tick.time(), tick.tickAttribBidAsk(), tick.priceBid(), tick.priceAsk(), tick.sizeBid(), tick.sizeAsk()));
//		return new Candle(tick.time(), tick.time(), tick.price(), tick.price(), tick.price(), tick.price(), tick.size());
		return null;
	}

	@Override
	public final void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		TradingBook depthDialog = marketBooks.get(tickerId);
		if (depthDialog != null) {
			depthDialog.updateBook(tickerId, position, "", operation, side, price, size);
		} else {
			log.warn("No book information associated with request {}", tickerId);
		}
	}

	@Override
	public final void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size, boolean isSmartDepth) {
		TradingBook book;

		if (isSmartDepth) {
			book = smartBooks.get(tickerId);
		} else {
			book = marketBooks.get(tickerId);
		}
		if (book != null) {
			book.updateBook(tickerId, position, marketMaker, operation, side, price, size);
		} else {
			log.warn("No book information associated with request {}", tickerId);
		}
	}

	@Override
	public final void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean last) {
		requestHandler.handleResponse(reqId, last, ticks,
				ResponseProcessor::translateHistoricalTick,
				(t) -> EWrapperMsgGenerator.historicalTick(reqId, t.time(), t.price(), t.size()));
	}

	@Override
	public final void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
		requestHandler.handleResponse(reqId, done, ticks,
				ResponseProcessor::translateHistoricalTickLast,
				(tick) -> EWrapperMsgGenerator.historicalTickLast(reqId, tick.time(), tick.tickAttribLast(), tick.price(), tick.size(), tick.exchange(), tick.specialConditions()));
	}

	@Override
	public final void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
		log.warn("Ignoring response for request ID: [{}] BID_ASK candles not supported.", reqId);
//		requestHandler.handleResponse(reqId, ticks,
//				ResponseProcessor::translateHistoricalTickBidAsk,
//				(tick) -> EWrapperMsgGenerator.historicalTickBidAsk(reqId, tick.time(), tick.tickAttribBidAsk(), tick.priceBid(), tick.priceAsk(), tick.sizeBid(), tick.sizeAsk()));
	}

	public final void historicalData(int reqId, Bar bar) {
		requestHandler.handleResponse(reqId, translate(bar),
				() -> EWrapperMsgGenerator.historicalData(reqId, bar.time(), bar.open(), bar.high(), bar.low(), bar.close(), bar.volume(), bar.count(), bar.wap()));
	}

	public final void historicalDataEnd(int reqId, String startDate, String endDate) {
		log.debug(EWrapperMsgGenerator.historicalDataEnd(reqId, startDate, endDate));
		requestHandler.closeOpenFeed(reqId);
	}

	@Override
	public final void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
		log.info(EWrapperMsgGenerator.historicalNews(requestId, time, providerCode, articleId, headline));
	}

	@Override
	public final void historicalNewsEnd(int requestId, boolean hasMore) {
		log.info(EWrapperMsgGenerator.historicalNewsEnd(requestId, hasMore));
	}

	@Override
	public final void headTimestamp(int reqId, String headTimestamp) {
		log.info(EWrapperMsgGenerator.headTimestamp(reqId, headTimestamp));
	}

	@Override
	public final void histogramData(int reqId, List<HistogramEntry> items) {
		log.info(EWrapperMsgGenerator.histogramData(reqId, items));
	}


	public final void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
		requestHandler.handleResponse(reqId, new Candle(time, time, open, high, low, close, volume),
				() -> EWrapperMsgGenerator.realtimeBar(reqId, time, open, high, low, close, volume, wap, count));
	}

	public final void error(Exception ex) {
		if (!disconnecting) {
			log.error(EWrapperMsgGenerator.error(ex), ex);
		}
	}

	public final void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		// received order status
		log.info(EWrapperMsgGenerator.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice));

		// make sure id for next order is at least orderId+1
		requestHandler.setNextOrderId(orderId + 1);
	}

	public final void contractDetails(int reqId, ContractDetails contractDetails) {
		requestHandler.handleResponse(reqId, translate(contractDetails),
				() -> EWrapperMsgGenerator.contractDetails(reqId, contractDetails));
	}

	private Candle translate(Bar bar) {
		long time = requestHandler.formattedDateToMillis(bar.time());
		return new Candle(time, time, bar.open(), bar.high(), bar.low(), bar.close(), bar.volume());
	}

	private SymbolInformation translate(ContractDetails contractDetails) {
		Contract contract = contractDetails.contract();
		SymbolInformation out = new SymbolInformation(contract.symbol());

		out.priceDecimalPlaces(Utils.countDecimals(contractDetails.minTick()));
		out.quantityDecimalPlaces(8);
//		out.minimumAssetsPerOrder(?);

		return out;
	}

	public final void contractDetailsEnd(int reqId) {
		requestHandler.responseFinalized(reqId);
	}

	@Override
	public final void error(int id, int errorCode, String errorMsg) {
		requestHandler.responseFinalizedWithError(id, errorCode, errorMsg);
	}

	@Override
	public final void nextValidId(int orderId) {
		log.debug(EWrapperMsgGenerator.nextValidId(orderId));
		requestHandler.setNextOrderId(orderId);
	}

	public final void updateAccountValue(String key, String value, String currency, String accountName) {
		log.info("Account {} updated. Key: {}, Value {}, Currency {}", accountName, key, value, currency);
		accountBalance.updateAccountValue(key, value, currency, accountName);
	}

	public final void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		accountBalance.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName);
	}
}
