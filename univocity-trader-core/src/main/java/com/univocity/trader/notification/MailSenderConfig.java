package com.univocity.trader.notification;


import org.apache.commons.lang3.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MailSenderConfig {

	private String smtpHost;
	private boolean smtpTlsSsl;
	private int smtpPort;
	private String smtpUsername;
	private char[] smtpPassword;
	private String smtpSender = null;
	private String replyToAddress;

	public MailSenderConfig() {
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public boolean isSmtpTlsSsl() {
		return smtpTlsSsl;
	}

	public void setSmtpTlsSsl(boolean smtpTlsSsl) {
		this.smtpTlsSsl = smtpTlsSsl;
	}

	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpUsername() {
		return smtpUsername;
	}

	public void setSmtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
	}

	public char[] getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(char[] smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public String getSmtpSender() {
		return smtpSender;
	}

	public void setSmtpSender(String smtpSender) {
		this.smtpSender = smtpSender;
	}

	public String getReplyToAddress() {
		return replyToAddress;
	}

	public void setReplyToAddress(String replyToAddress) {
		this.replyToAddress = replyToAddress;
	}

	public boolean isConfigured(){
		return StringUtils.isNoneBlank(getSmtpUsername(), getSmtpSender(), getSmtpHost()) && ArrayUtils.isNotEmpty(getSmtpPassword());
	}
}
