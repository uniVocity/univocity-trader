package com.univocity.trader.notification;

import com.sun.nio.sctp.*;
import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.config.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import static com.univocity.trader.account.Balance.*;


public class OrderExecutionToEmail implements OrderListener {

	private static final Logger log = LoggerFactory.getLogger(NotificationHandler.class);

	private SmtpMailSender mailSender;
	private TradingManager tradingManager;

	private String referenceCurrencySymbol;

	public OrderExecutionToEmail() {
		this((SmtpMailSender) null);
	}

	public OrderExecutionToEmail(SmtpMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public OrderExecutionToEmail(EmailConfiguration mailSenderConfig) {
		this(new SmtpMailSender(mailSenderConfig));
	}

	public void initialize(TradingManager tradingManager) {
		this.tradingManager = tradingManager;
		this.referenceCurrencySymbol = tradingManager.getReferenceCurrencySymbol();
	}

	public void sendBalanceEmail(String title, Client client) {
		if (mailSender == null) {
			return;
		}
		try {
			Email email = new Email();
			email.setFrom(mailSender.getSenderAddress());
			email.setTitle(title);

			Map<String, Balance> balances = tradingManager.updateBalances();
			String body = printTotalBalances(balances);

			email.setBody(body);
			email.setTo(new String[]{client.getEmail()});
			mailSender.sendEmailViaSmtp(email);
		} catch (Exception e) {
			log.error("Error sending balance e-mail", e);
		}
	}

	private synchronized String printTotalBalances(Map<String, Balance> balances) {
		SymbolPriceDetails f = tradingManager.getPriceDetails();
		StringBuilder out = new StringBuilder("\nActual balances:\n");
		var total = new AtomicReference<>(0.0);
		printTotalBalances(balances, new HashSet<>(), out, total, this.tradingManager);
		out.append("\n\t* ").append(f.priceToString(tradingManager.getCash())).append(' ').append(tradingManager.getFundSymbol());
		double holdings = total.get() + balances.getOrDefault(referenceCurrencySymbol, ZERO).getTotal();
		out.append("\n\nApproximate holdings ~$").append(f.switchToSymbol(referenceCurrencySymbol).priceToString(holdings)).append(" ").append(referenceCurrencySymbol);
		return out.toString();
	}

	private void sendEmail(Order order, Trade trade, Client client) {
		if (mailSender == null) {
			return;
		}
		Trader trader = trade.trader();

		String assetSymbol = trader.assetSymbol();
		String fundSymbol = trader.fundSymbol();
		try {
			SymbolPriceDetails f = trader.priceDetails();
			String balances = printTotalBalances(tradingManager.updateBalances());

			String timeLong = " at " + trader.latestCandle().getFormattedCloseTime("h:mma, MMMM dd, yyyy", client.getTimezone());
			String timeShort = " - " + trader.latestCandle().getFormattedCloseTime("EEEE hh:mma", client.getTimezone());
			String title = order.getSide() + " " + assetSymbol;

			if (!order.isFinalized()) {
				title += " (PENDING)";
			} else {
				double fillPct = order.getFillPct();
				if (fillPct > 98.0) {
					title += " (FILLED)";
				} else if (fillPct < 1.0) {
					title += " (CANCELLED)";
				} else {
					title += " (" + order.getFormattedFillPct() + " filled, CANCELLED)";
				}
			}
			String typeDescription = order.isLimit() ? "with limit order of " + f.priceToString(order.getPrice()) + " " + fundSymbol + " per unit" : "at market";
			String details;

			String qty = f.quantityToString(order.getQuantity());

			if (order.isBuy()) {
				title += " @ " + f.priceToString(order.getPrice()) + timeShort;
				details = "Bought " + qty + " " + assetSymbol + " " + typeDescription + " when price reached " + f.priceToString(trader.lastClosingPrice()) + " " + fundSymbol + timeLong + ".";
				details += "\nAmount invested: $" + f.priceToString(order.getTotalOrderAmount()) + " " + fundSymbol;
				details += "\nOrder status: " + order.getStatus();
			} else {
				title += " @ " + f.priceToString(order.getPrice()) + " (" + trade.formattedPriceChangePct(order.getPrice()) + ")" + timeShort;
				details = "Sold " + qty + " " + assetSymbol + " " + typeDescription + " when price reached " + f.priceToString(trader.lastClosingPrice()) + " " + fundSymbol + timeLong + ".";
				if(trade.exitReason() != null) {
					details += "\nExit reason: " + trade.exitReason();
				}
				details += "\nOrder status: " + order.getStatus();
				details += "\n\nCurrent price change: " + trade.formattedPriceChangePct();

				if (order.isFinalized()) {
					details += "\nProfit/loss: ";
				} else {
					details += "\nExpected profit/loss: ";
				}
				details += f.priceToString(trade.actualProfitLoss()) + " (" + trade.formattedProfitLossPct() + ")";

				details += "\nClose price: " + f.priceToString(trader.lastClosingPrice()) + " " + fundSymbol;
				details += "\nTrade length: " + trade.formattedTradeLength();
				details += "\nMinimum price: " + f.priceToString(trade.minPrice()) + " " + fundSymbol + " (" + trade.formattedMinChangePct() + ")";
				details += "\nMaximum price: " + f.priceToString(trade.maxPrice()) + " " + fundSymbol + " (" + trade.formattedMaxChangePct() + ")";
			}

			String body = details + "\n" + balances;

			Email email = new Email();
			email.setFrom(mailSender.getSenderAddress());
			email.setTitle(title);
			email.setBody(body);

			email.setTo(new String[]{client.getEmail()});
			mailSender.sendEmailViaSmtp(email);
		} catch (Exception e) {
			log.error("Error sending trade notification e-mail", e);
		}
	}

	@Override
	public void orderSubmitted(Order order, Trade trade, Client client) {
		if (!order.isFinalized()) { //skip this one and let orderFinalized() run
			sendEmail(order, trade, client);
		}
	}

	@Override
	public void orderFinalized(Order order, Trade trade, Client client) {
		sendEmail(order, trade, client);
	}

	private void printTotalBalances(Map<String, Balance> balances, Set<TradingManager> visited, StringBuilder msg, AtomicReference<Double> total, TradingManager next) {
		if (visited.contains(next)) {
			return;
		}
		visited.add(next);

		Balance instrument = balances.getOrDefault(next.getAssetSymbol(), ZERO);

		double assets = instrument.getFree();
		double locked = instrument.getLocked();

		if (assets != 0 || locked != 0) {
			SymbolPriceDetails f = next.getPriceDetails();
			double lastPrice = next.getLatestPrice();
			boolean printing = false;
			if (assets > 0.0) {
				Trader trader = next.getTrader();
				double worth = assets * lastPrice;
				total.set(total.get() + convertToReferenceCurrency(worth, next));
				if (worth > 0.5) {
					printing = true;
					msg.append("\n\t* ").append(f.quantityToString(assets)).append(" ").append(next.getAssetSymbol());
					msg.append(", ");
					msg.append("trading at ").append(f.priceToString(trader.lastClosingPrice()));
					msg.append(". Holding ").append(f.quantityToString(trader.assetQuantity())).append(" units");
					msg.append(", worth ~").append(f.switchToSymbol(trader.assetSymbol() + referenceCurrencySymbol).priceToString(worth)).append(" ").append(next.getFundSymbol());

					Set<Trade> trades = trader.trades();
					if(!trades.isEmpty()){
						String indent = ". ";
						if(trades.size() > 1){
							msg.append(". Trades:");
							indent = ".\n\t\t + ";
						}
						for(Trade trade : trades) {
							msg.append(indent);
							if (trade.averagePrice() > 0) {
								msg.append("Paid ").append(f.priceToString(trade.averagePrice()));
							}
							double change = trade.priceChangePct();
							if (change != 0.0) {
								msg.append(' ').append('(');
								if (change > 0.0) {
									msg.append('+');
								}
								msg.append(trade.formattedPriceChangePct());
								msg.append(')');
							}
							if (trade.ticks() > 0) {
								msg.append(" (max: ").append(f.priceToString(trade.maxPrice()))
										.append(", min: ").append(f.priceToString(trade.minPrice()))
										.append(", length: ").append(trade.formattedTradeLength())
										.append(")");
							}
						}
					}

				}
			}
			if (locked > 0.0) {
				if (assets > 0.0 && printing) {
					msg.append(" + ");
				} else {
					msg.append("\n\t* ");
				}
				total.set(total.get() + convertToReferenceCurrency(locked * lastPrice, next));
				msg.append(f.quantityToString(locked)).append(" ").append(next.getAssetSymbol()).append(" locked in order (worth ~").append(f.switchToSymbol(next.getAssetSymbol() + next.getReferenceCurrencySymbol()).priceToString(locked * lastPrice)).append(" ").append(next.getFundSymbol()).append(')');
			}
		}
		if (visited.size() <= 1) {
			for (TradingManager other : tradingManager.getAccount().getAllTradingManagers()) {
				printTotalBalances(balances, visited, msg, total, other);
			}
		}
	}

	private double convertToReferenceCurrency(double total, TradingManager tradingManager) {
		if (tradingManager.getFundSymbol().equalsIgnoreCase(referenceCurrencySymbol)) {
			return total;
		} else {
			return total * tradingManager.getLatestPrice(tradingManager.getAssetSymbol(), referenceCurrencySymbol);
		}
	}
}
