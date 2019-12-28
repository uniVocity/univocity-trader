package com.univocity.trader.notification;

import java.time.*;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface Client {

	String getId();

	void sendBalanceEmail(String title);

	void updateBalances();

	String getEmail();

	ZoneId getTimezone();

	Map<String, String[]> getSymbolPairs();
}
