package com.univocity.trader.config;

import java.util.*;
import java.util.function.*;

public class GenericConfiguration extends Configuration<GenericConfiguration, GenericAccountConfiguration>{

	@Override
	protected ConfigurationGroup[] getAdditionalConfigurationGroups() {
		return new ConfigurationGroup[0];
	}

	@Override
	protected GenericAccountConfiguration newAccountConfiguration(String id) {
		return new GenericAccountConfiguration(id);
	}
}