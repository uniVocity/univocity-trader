package com.univocity.trader.config;

import com.univocity.trader.account.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static com.univocity.trader.config.Allocation.*;
import static com.univocity.trader.config.Utils.*;

public class GenericAccountConfiguration extends AccountConfiguration<GenericAccountConfiguration> {

	public GenericAccountConfiguration(String id) {
		super(id);
	}
}
