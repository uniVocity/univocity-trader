package com.univocity.trader.exchange.interactivebrokers;

/**
 * Available tick types (to be used when receiving ticks in real time) as
 * described in: https://interactivebrokers.github.io/tws-api/classIBApi_1_1EClient.html#a3ab310450f1261accd706f69766b2263
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum TickType {
	Last,
	AllLast,
	BidAsk,
	MidPoint
}
