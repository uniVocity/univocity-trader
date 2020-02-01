package com.univocity.trader.config;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface ConfigurationGroup {

	void readProperties(PropertyBasedConfiguration properties);

	boolean isConfigured();
}
