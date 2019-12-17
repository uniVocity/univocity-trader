package com.univocity.trader.config;

import org.apache.commons.lang3.*;

import java.time.*;
import java.util.*;

public class AccountConfiguration<T extends AccountConfiguration<T>> {

	private static final Set<String> supportedTimeZones = new TreeSet<>(List.of(TimeZone.getAvailableIDs()));
	private static final String supportedTimezoneDescription;

	static {
		StringBuilder tmp = new StringBuilder(3000);
		tmp.append(" Supported timezones:");
		for (String tz : supportedTimeZones) {
			tmp.append('\n').append(tz);
		}
		supportedTimezoneDescription = tmp.toString();
	}

	private String email;
	private String referenceCurrency;
	private TimeZone timeZone;

	protected AccountConfiguration() {
	}

	protected final void readProperties(PropertyBasedConfiguration properties) {

	}

	protected final void readProperties(String accountId, PropertyBasedConfiguration properties) {
		if (!accountId.isBlank()) {
			accountId = accountId + ".";
		}
		email = properties.getProperty(accountId + "email");
		referenceCurrency = properties.getProperty(accountId + "reference.currency");

		String tz = properties.getProperty(accountId + "timezone");
		timeZone = getTimeZone(tz);
		if (timeZone == null) {
			String msg = "Unsupported timezone '" + tz + "' set ";
			if (accountId.isBlank()) {
				msg += "in 'timezone' property.";
			} else {
				msg += "for '" + accountId + "timezone' property.";
			}
			throw new IllegalConfigurationException(msg + supportedTimezoneDescription);
		}
		readAccountProperties(accountId, properties);
	}

	protected void readAccountProperties(String accountId, PropertyBasedConfiguration properties) {

	}

	public boolean isConfigured() {
		return StringUtils.isNoneBlank(referenceCurrency);
	}

	public String email() {
		return email;
	}

	public T email(String email) {
		this.email = email;
		return (T) this;
	}

	public String referenceCurrency() {
		return referenceCurrency;
	}

	public T referenceCurrency(String referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
		return (T) this;
	}

	public TimeZone timeZone() {
		return timeZone;
	}

	private TimeZone getTimeZone(String tz) {
		if (tz == null || tz.equals("system")) {
			timeZone = TimeZone.getDefault();
		} else if (supportedTimeZones.contains(tz)) {
			timeZone = TimeZone.getTimeZone(tz);
		}
		return null;
	}

	public T timeZone(String timeZone) {
		this.timeZone = getTimeZone(timeZone);
		if (this.timeZone == null) {
			throw new IllegalArgumentException("Unsupported time zone: '" + timeZone + "." + supportedTimezoneDescription);
		}
		return (T) this;
	}

	public T timeZone(ZoneId zoneId) {
		this.timeZone = TimeZone.getTimeZone(zoneId);
		return (T) this;
	}

	public T timeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		return (T) this;
	}
}