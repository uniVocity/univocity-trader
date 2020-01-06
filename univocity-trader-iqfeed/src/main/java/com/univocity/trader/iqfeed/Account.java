package com.univocity.trader.iqfeed;

import com.univocity.trader.config.*;
import com.univocity.trader.iqfeed.api.constant.*;
import org.apache.commons.lang3.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Account extends AccountConfiguration<Account> {

	private String iqPortalPath = IQFeedApiConstants.IQPORTAL_PATH;
	private String host;
	private int port;
	private String product;
	private String version;
	private String login;
	private String pass;

	public Account(String id) {
		super(id);

		//TODO: list more 100% required property names here.
		addRequiredPropertyNames("iq.login");
	}

	@Override
	protected void readExchangeAccountProperties(String accountId, PropertyBasedConfiguration properties) {
		this.iqPortalPath = properties.getOptionalProperty(accountId + ".iq.portal.path");
		this.host = properties.getOptionalProperty(accountId + "iq.host");
		this.port = properties.getInteger(accountId + "iq.port", 0); //TODO: provide a sane default instead of 0
		this.product = properties.getOptionalProperty(accountId + "iq.product");
		this.version = properties.getOptionalProperty(accountId + "iq.version");
		this.login = properties.getOptionalProperty(accountId + "iq.login");
		this.pass = properties.getOptionalProperty(accountId + "iq.pass");
	}


	public String iqPortalPath() {
		return iqPortalPath;
	}

	public Account iqPortalPath(String iqPortalPath) {
		this.iqPortalPath = iqPortalPath;
		return this;
	}

	public String host() {
		return host;
	}

	public Account host(String host) {
		this.host = host;
		return this;
	}

	public int port() {
		return port;
	}

	public Account port(int port) {
		this.port = port;
		return this;
	}

	public String product() {
		return product;
	}

	public Account product(String product) {
		this.product = product;
		return this;
	}

	public String version() {
		return version;
	}

	public Account version(String version) {
		this.version = version;
		return this;
	}

	public String login() {
		return login;
	}

	public Account login(String login) {
		this.login = login;
		return this;
	}

	public String pass() {
		return pass;
	}

	public Account pass(String pass) {
		this.pass = pass;
		return this;
	}

	@Override
	public boolean isConfigured() {
		return super.isConfigured() && StringUtils.isNoneBlank(login);
	}
}