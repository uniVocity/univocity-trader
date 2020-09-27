package com.univocity.trader.exchange.interactivebrokers.api;

import com.ib.client.*;
import org.slf4j.*;

import java.util.*;

/**
 *  {@link EWrapper} implementation of methods that are simply logged and are currently not used/ignored.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
abstract class IgnoredResponseProcessor implements EWrapper {

	private static final Logger log = LoggerFactory.getLogger(IgnoredResponseProcessor.class);

	@Override
	public final void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
		log.info(EWrapperMsgGenerator.marketRule(marketRuleId, priceIncrements));
		//TODO: populate symbol information here
	}


	@Override
	public final void historicalDataUpdate(int reqId, Bar bar) {
		historicalData(reqId, bar);
	}

	@Override
	public final void rerouteMktDataReq(int reqId, int conId, String exchange) {
		log.info(EWrapperMsgGenerator.rerouteMktDataReq(reqId, conId, exchange));
	}

	@Override
	public final void rerouteMktDepthReq(int reqId, int conId, String exchange) {
		log.info(EWrapperMsgGenerator.rerouteMktDepthReq(reqId, conId, exchange));
	}


	@Override
	public final void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
		log.info(EWrapperMsgGenerator.pnl(reqId, dailyPnL, unrealizedPnL, realizedPnL));
	}

	@Override
	public final void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
		log.info(EWrapperMsgGenerator.pnlSingle(reqId, pos, dailyPnL, unrealizedPnL, realizedPnL, value));
	}

	@Override
	public final void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast, String exchange, String specialConditions) {
		log.info(EWrapperMsgGenerator.tickByTickAllLast(reqId, tickType, time, price, size, tickAttribLast, exchange, specialConditions));
	}

	@Override
	public final void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize, TickAttribBidAsk tickAttribBidAsk) {
		log.info(EWrapperMsgGenerator.tickByTickBidAsk(reqId, time, bidPrice, askPrice, bidSize, askSize, tickAttribBidAsk));
	}

	@Override
	public final void orderBound(long orderId, int apiClientId, int apiOrderId) {
		log.info(EWrapperMsgGenerator.orderBound(orderId, apiClientId, apiOrderId));
	}

	@Override
	public final void completedOrder(Contract contract, Order order, OrderState orderState) {
		log.info(EWrapperMsgGenerator.completedOrder(contract, order, orderState));
	}

	@Override
	public final void completedOrdersEnd() {
		log.info(EWrapperMsgGenerator.completedOrdersEnd());
	}

	@Override
	public final void newsProviders(NewsProvider[] newsProviders) {
		log.info("Got {} news providers", newsProviders.length);
	}

	@Override
	public final void newsArticle(int i, int i1, String s) {
		log.info("News article {} {}: {}", i, i1, s);
	}

	@Override
	public final void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {
		log.info(EWrapperMsgGenerator.securityDefinitionOptionalParameter(reqId, exchange, underlyingConId, tradingClass, multiplier, expirations, strikes));
	}

	@Override
	public final void securityDefinitionOptionalParameterEnd(int reqId) {
	}

	@Override
	public final void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
		log.info(EWrapperMsgGenerator.softDollarTiers(tiers));
	}

	@Override
	public final void familyCodes(FamilyCode[] familyCodes) {
		log.info(EWrapperMsgGenerator.familyCodes(familyCodes));
	}

	@Override
	public final void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
		log.info(EWrapperMsgGenerator.symbolSamples(reqId, contractDescriptions));
	}

	@Override
	public final void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
		log.info(EWrapperMsgGenerator.mktDepthExchanges(depthMktDataDescriptions));
	}

	@Override
	public final void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {
		log.info(EWrapperMsgGenerator.tickNews(tickerId, timeStamp, providerCode, articleId, headline, extraData));
	}

	@Override
	public final void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {
		log.info(EWrapperMsgGenerator.smartComponents(reqId, theMap));
	}

	@Override
	public final void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		log.info(EWrapperMsgGenerator.tickReqParams(tickerId, minTick, bboExchange, snapshotPermissions));
	}

	public final void verifyMessageAPI(String apiData) {
		log.debug(apiData);
	}

	public final void verifyCompleted(boolean isSuccessful, String errorText) {
		if (!isSuccessful) {
			log.error(errorText);
		}
	}

	public final void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
		log.debug(apiData + ". Challenge: " + xyzChallenge);
	}

	public final void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
		if (!isSuccessful) {
			log.error(errorText);
		}
	}

	public final void marketDataType(int reqId, int marketDataType) {
		log.info(EWrapperMsgGenerator.marketDataType(reqId, marketDataType));
	}

	public final void commissionReport(CommissionReport commissionReport) {
		log.info(EWrapperMsgGenerator.commissionReport(commissionReport));
	}

	public final void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {
		log.info(EWrapperMsgGenerator.positionMulti(reqId, account, modelCode, contract, pos, avgCost));
	}

	public final void positionMultiEnd(int reqId) {
		log.info(EWrapperMsgGenerator.positionMultiEnd(reqId));
	}

	public final void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {
		log.info(EWrapperMsgGenerator.accountUpdateMulti(reqId, account, modelCode, key, value, currency));
	}

	public final void accountUpdateMultiEnd(int reqId) {
		log.info(EWrapperMsgGenerator.accountUpdateMultiEnd(reqId));
	}

	public final void receiveFA(int faDataType, String xml) {
		log.info("Received financial advisor type {} XML : {}", faDataType, xml);
	}

	public final void currentTime(long time) {
		log.info(EWrapperMsgGenerator.currentTime(time));
	}

	public final void fundamentalData(int reqId, String data) {
		log.info(EWrapperMsgGenerator.fundamentalData(reqId, data));
	}

	public final void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
		log.info(EWrapperMsgGenerator.deltaNeutralValidation(reqId, deltaNeutralContract));
	}

	public final void scannerParameters(String xml) {
		log.debug("Received scanner parameters XML {}", xml);
	}

	public final void error(String str) {
		log.error(EWrapperMsgGenerator.error(str));
	}

	public final void displayGroupList(int reqId, String groups) {
		log.info("Group list (request ID: {}): {}}", reqId, groups);
	}

	public final void displayGroupUpdated(int reqId, String contractInfo) {
		log.info("Group updated (request ID: {}): {}}", reqId, contractInfo);
	}


	public final void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {
		log.info(EWrapperMsgGenerator.tickPrice(tickerId, field, price, attribs));
	}

	public final void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
		// received computation tick
		log.info(EWrapperMsgGenerator.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice));
	}

	public final void tickSize(int tickerId, int field, int size) {
		// received size tick
		log.info(EWrapperMsgGenerator.tickSize(tickerId, field, size));
	}

	public final void tickGeneric(int tickerId, int tickType, double value) {
		// received generic tick
		log.info(EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value));
	}

	public final void tickString(int tickerId, int tickType, String value) {
		// received String tick
		log.info(EWrapperMsgGenerator.tickString(tickerId, tickType, value));
	}

	public final void tickSnapshotEnd(int tickerId) {
		log.info(EWrapperMsgGenerator.tickSnapshotEnd(tickerId));
	}

	public final void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate) {
		// received EFP tick
		log.info(EWrapperMsgGenerator.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureLastTradeDate, dividendImpact, dividendsToLastTradeDate));
	}

	public final void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		// received open order
		log.info(EWrapperMsgGenerator.openOrder(orderId, contract, order, orderState));
	}

	public final void openOrderEnd() {
		// received open order end
		log.info(EWrapperMsgGenerator.openOrderEnd());
	}

	public final void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
		log.info(EWrapperMsgGenerator.scannerData(reqId, rank, contractDetails, distance,benchmark, projection, legsStr));
	}

	public final void scannerDataEnd(int reqId) {
		log.info(EWrapperMsgGenerator.scannerDataEnd(reqId));
	}

	public final void bondContractDetails(int reqId, ContractDetails contractDetails) {
		log.info(EWrapperMsgGenerator.bondContractDetails(reqId, contractDetails));
	}

	public final void execDetails(int reqId, Contract contract, Execution execution) {
		log.info(EWrapperMsgGenerator.execDetails(reqId, contract, execution));
	}

	public final void execDetailsEnd(int reqId) {
		log.info(EWrapperMsgGenerator.execDetailsEnd(reqId));
	}

	public final void updateAccountTime(String timeStamp) {
		log.info("Account time: {}", timeStamp);
	}

	public final void accountDownloadEnd(String accountName) {
		log.info(EWrapperMsgGenerator.accountDownloadEnd(accountName));
	}

	public final void connectionClosed() {
		log.info(EWrapperMsgGenerator.connectionClosed());
	}

	@Override
	public final void managedAccounts(String accountsList) {
		log.info(EWrapperMsgGenerator.managedAccounts(accountsList));
	}

	@Override
	public final void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		log.info(EWrapperMsgGenerator.updateNewsBulletin(msgId, msgType, message, origExchange));
	}
}
