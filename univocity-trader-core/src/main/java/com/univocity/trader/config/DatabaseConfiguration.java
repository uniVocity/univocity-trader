package com.univocity.trader.config;

import org.apache.commons.lang3.*;

public class DatabaseConfiguration extends ConfigurationGroup {

	private String jdbcUrl;
	private String user;
	private char[] password;
	private String jdbcDriver;

	DatabaseConfiguration(Configuration parent) {
		super(parent);
	}

	@Override
	void readProperties(PropertyBasedConfiguration properties) {
		jdbcDriver = properties.getProperty("database.jdbc.driver");
		jdbcUrl = properties.getProperty("database.jdbc.url");
		user = properties.getProperty("database.user");

		String pwd = properties.getProperty("database.password");
		password = pwd == null ? null : pwd.toCharArray();
	}

	public String jdbcUrl() {
		return jdbcUrl;
	}

	public DatabaseConfiguration jdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
		return this;
	}

	public String user() {
		return user;
	}

	public DatabaseConfiguration user(String user) {
		this.user = user;
		return this;
	}

	public char[] password() {
		return password;

	}

	public DatabaseConfiguration password(String password) {
		return password(password == null ? null : password.toCharArray());
	}

	public DatabaseConfiguration password(char[] password) {
		this.password = password;
		return this;
	}

	public String jdbcDriver() {
		return jdbcDriver;
	}

	public DatabaseConfiguration jdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
		return this;
	}

	@Override
	public boolean isConfigured() {
		return StringUtils.isNoneBlank(jdbcUrl, jdbcDriver, user);
	}
}