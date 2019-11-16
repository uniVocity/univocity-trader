package com.univocity.trader.notification;

import com.sun.nio.sctp.*;
import com.univocity.trader.*;
import com.univocity.trader.account.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import static com.univocity.trader.account.Balance.*;


public class OrderExecutionToEmail implements OrderEventListener {

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

	public OrderExecutionToEmail(MailSenderConfig mailSenderConfig) {
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
		double holdings = total.get() + balances.getOrDefault(referenceCurrencySymbol, ZERO).getTotal().doubleValue();
		out.append("\n\nApproximate holdings ~$").append(f.switchToSymbol(tradingManager.getAssetSymbol() + referenceCurrencySymbol).priceToString(holdings)).append(" ").append(referenceCurrencySymbol);
		return out.toString();
	}

	@Override
	public void onOrderUpdate(Order order, Trader trader, Client client) {
		if (mailSender == null) {
			return;
		}

		String assetSymbol = trader.getAssetSymbol();
		String fundSymbol = trader.getFundSymbol();
		try {
			SymbolPriceDetails f = trader.getPriceDetails();
			String balances = printTotalBalances(tradingManager.updateBalances());

			String timeLong = " at " + trader.getCandle().getFormattedCloseTime("h:mma, MMMM dd, yyyy", client.getTimezone());
			String timeShort = " - " + trader.getCandle().getFormattedCloseTime("EEEE hh:mma", client.getTimezone());
			String title = order.getSide() + " " + assetSymbol;
			String typeDescription = order.getType() == Order.Type.LIMIT ? "with limit order of " + f.priceToString(order.getPrice()) + " " + fundSymbol + " per unit" : "at market";
			String details;

			String qty = f.quantityToString(order.getQuantity());

			if (order.getSide() == Order.Side.BUY) {
				title += " @ " + f.priceToString(order.getPrice()) + timeShort;
				details = "Bought " + qty + " " + assetSymbol + " " + typeDescription + " when price reached " + f.priceToString(trader.getLastClosingPrice()) + " " + fundSymbol + timeLong + ".";
				details += "\nAmount invested: $" + f.priceToString(order.getTotalOrderAmount()) + " " + fundSymbol;
				details += "\nOrder status: " + order.getStatus();
			} else {
				title += " @ " + f.priceToString(order.getPrice()) + " (" + trader.getFormattedPriceChangePct(order.getPrice()) + ")" + timeShort;
				details = "Sold " + qty + " " + assetSymbol + " " + typeDescription + " when price reached " + f.priceToString(trader.getLastClosingPrice()) + " " + fundSymbol + timeLong + ".";
				details += "\nExit reason: " + trader.exitReason();
				details += "\nOrder status: " + order.getStatus();
				details += "\n\nChange: " + trader.getFormattedPriceChangePct();
				details += "\nClose price: " + f.priceToString(trader.getLastClosingPrice()) + " " + fundSymbol;
				details += "\nTrade length: " + trader.getFormattedTradeLength();
				details += "\nMinimum price: " + f.priceToString(trader.getMinPrice()) + " " + fundSymbol + " (" + trader.getFormattedMinChangePct() + ")";
				details += "\nMaximum price: " + f.priceToString(trader.getMaxPrice()) + " " + fundSymbol + " (" + trader.getFormattedMaxChangePct() + ")";
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
//
//	public void notifyTradeExecution(Order order, Trader trader, Client client) {
//		if (notifiers != null) {
//			for(int i = 0; i < notifiers.length; i++){
//				notifiers[i].orderUpdated(order, trader, client);
//			}
//		}
//	}

	private void printTotalBalances(Map<String, Balance> balances, Set<TradingManager> visited, StringBuilder msg, AtomicReference<Double> total, TradingManager next) {
		if (visited.contains(next)) {
			return;
		}
		visited.add(next);

		Balance instrument = balances.getOrDefault(next.getAssetSymbol(), ZERO);

		double assets = instrument.getFree().doubleValue();
		double locked = instrument.getLocked().doubleValue();

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
					if (trader.getBoughtPrice() > 0) {
						msg.append(". Paid ").append(f.priceToString(trader.getBoughtPrice()));
					}
					msg.append(", ");
					msg.append("trading at ").append(f.priceToString(trader.getLastClosingPrice()));
					double change = trader.getChange();
					if (change != 0.0) {
						msg.append(' ').append('(');
						if (change > 0.0) {
							msg.append('+');
						}
						msg.append(trader.getFormattedPriceChangePct());
						msg.append(')');
					}
					msg.append(". Holding ").append(f.quantityToString(trader.getAssetQuantity())).append(" units");
					msg.append(", worth ~").append(f.switchToSymbol(trader.getAssetSymbol() + referenceCurrencySymbol).priceToString(worth)).append(" ").append(next.getFundSymbol());
					if (trader.getTicks() > 0) {
						msg.append(" (max: ").append(f.priceToString(trader.getMaxPrice()))
								.append(", min: ").append(f.priceToString(trader.getMinPrice()))
								.append(", length: ").append(trader.getFormattedTradeLength())
								.append(")");
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
