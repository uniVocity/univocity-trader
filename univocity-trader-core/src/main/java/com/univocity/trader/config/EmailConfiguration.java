package com.univocity.trader.config;

import org.apache.commons.lang3.*;

public class EmailConfiguration extends ConfigurationGroup {

	private String replyToAddress;
	private int smtpPort;
	private String smtpHost;
	private boolean smtpSSL;
	private String smtpUsername;
	private char[] smtpPassword;
	private String smtpSender = null;

	EmailConfiguration(UnivocityConfiguration parent) {
		super(parent);
	}

	@Override
	void readProperties(PropertyBasedConfiguration properties) {
		replyToAddress = properties.getProperty("mail.reply.to");
		smtpHost = properties.getProperty("mail.smtp.host");
		smtpSSL = properties.getBoolean("mail.smtp.ssl", false);
		smtpPort = properties.getInteger("mail.smtp.port", 0);
		smtpUsername = properties.getProperty("mail.smtp.username");
		smtpSender = properties.getProperty("mail.smtp.sender");

		String pwd = properties.getProperty("mail.smtp.password");
		smtpPassword = pwd == null ? null : pwd.toCharArray();
	}

	public int smtpPort() {
		return smtpPort;
	}

	public String replyToAddress() {
		return replyToAddress;
	}

	public String smtpSender() {
		return smtpSender;
	}

	public String smtpHost() {
		return smtpHost;
	}

	public boolean smtpSSL() {
		return smtpSSL;
	}

	public EmailConfiguration replyToAddress(String replyToAddress) {
		this.replyToAddress = replyToAddress;
		return this;
	}

	public EmailConfiguration smtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
		return this;
	}

	public EmailConfiguration smtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
		return this;
	}


	public EmailConfiguration smtpSender(String smtpSender) {
		this.smtpSender = smtpSender;
		return this;
	}

	public EmailConfiguration smtpSSL(boolean smtpSSL) {
		this.smtpSSL = smtpSSL;
		return this;
	}

	public EmailConfiguration smtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
		return this;
	}

	public EmailConfiguration smtpPassword(String smtpPassword) {
		return smtpPassword(smtpPassword.toCharArray());
	}

	public EmailConfiguration smtpPassword(char[] smtpPassword) {
		this.smtpPassword = smtpPassword;
		return this;
	}

	public char[] smtpPassword() {
		return smtpPassword;
	}

	public String smtpUsername() {
		return smtpUsername;
	}

	@Override
	public boolean isConfigured() {
		return StringUtils.isNoneBlank(smtpUsername, smtpSender, smtpHost) && ArrayUtils.isNotEmpty(smtpPassword);
	}
}