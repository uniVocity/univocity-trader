package com.univocity.trader.config;

import java.util.*;
import java.util.function.*;

public class ClientList<T extends ClientConfiguration<T>> extends ConfigurationGroup {

	private Map<String, T> clients = new LinkedHashMap<>();
	private final Supplier<T> clientConfigurationSupplier;

	ClientList(ConfigurationRoot parent, Supplier<T> clientConfigurationSupplier) {
		super(parent);
		this.clientConfigurationSupplier = clientConfigurationSupplier;
	}

	@Override
	protected void readProperties(PropertyBasedConfiguration properties) {
		List<String> clientIds = properties.getOptionalList("clients");

		if (!clientIds.isEmpty()) {
			for (String clientId : clientIds) {
				if (clients.containsKey(clientId)) {
					throw new IllegalConfigurationException("Duplicate client ID in properties file: " + clientId);
				}
				client(clientId).readProperties(clientId, properties);
			}
		}
		//always assume a client without ID. We'll get rid of it if not configured
		client().readProperties("", properties);
	}

	@Override
	public boolean isConfigured() {
		return !clients.isEmpty() && clients.values().stream().anyMatch(c -> c.isConfigured());
	}

	public final T client(String clientId) {
		return clients.computeIfAbsent(clientId, c -> clientConfigurationSupplier.get());
	}

	public final T client() {
		return client("");
	}
}