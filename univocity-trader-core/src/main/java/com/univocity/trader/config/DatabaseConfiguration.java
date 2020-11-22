package com.univocity.trader.config;

import org.apache.commons.lang3.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.*;

import javax.sql.*;
import java.util.function.*;

public class DatabaseConfiguration implements ConfigurationGroup {

	private String jdbcUrl;
	private String user;
	private char[] password;
	private String jdbcDriver;
	private Supplier<DataSource> dataSource;
	private final ThreadLocal<JdbcTemplate> db = ThreadLocal.withInitial(() -> new JdbcTemplate(dataSource()));

	@Override
	public void readProperties(PropertyBasedConfiguration properties) {
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
		return dataSource != null || StringUtils.isNoneBlank(jdbcUrl, jdbcDriver, user);
	}

	public DataSource dataSource() {
		if(dataSource == null){
			return defaultDataSource();
		}
		return dataSource.get();
	}

	private DataSource getDataSource() {
		if(dataSource == null){
			return defaultDataSource();
		}
		return dataSource.get();
	}

	public void dataSource(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("Data source can't be null");
		}
		dataSource(() -> dataSource);
	}

	public void dataSource(Supplier<DataSource> dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("Data source can't be null");
		}
		if (dataSource.get() == null) {
			throw new IllegalArgumentException("Data source supplier can't return null");
		}
		this.dataSource = dataSource;
	}

	private DataSource defaultDataSource() {
		if (!isConfigured()) {
			jdbcDriver("com.mysql.jdbc.Driver")
					.jdbcUrl("jdbc:mysql://localhost:3306/trading?autoReconnect=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true")
					.user("root");
		}

		try {
			Class.forName(jdbcDriver());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		SingleConnectionDataSource ds = new SingleConnectionDataSource();
		ds.setUrl(jdbcUrl());
		ds.setUsername(user());
		if (password() != null) {
			ds.setPassword(new String(password()));
		}
		ds.setSuppressClose(true);
		return ds;
	}
}