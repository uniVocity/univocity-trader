package com.univocity.trader.utils;

import com.univocity.trader.simulation.*;
import org.apache.commons.lang3.*;

import java.util.*;
import java.util.function.*;

public class NewInstances<T> implements InstancesProvider<T> {

	private static final Set<Object> allInstances = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

	private final T[] empty;
	private final List<InstanceProvider<T>> providers = new ArrayList<>();

	public NewInstances(T[] empty) {
		this.empty = empty;
	}

	public final NewInstances<T> add(Supplier<T> instanceSupplier) {
		add((s, p) -> instanceSupplier.get());
		return this;
	}

	public final NewInstances<T> add(Function<String, T> nonParameterizedInstance) {
		add((s, p) -> nonParameterizedInstance.apply(s));
		return this;
	}

	public final NewInstances<T> add(InstanceProvider<T> parameterizedInstance) {
		providers.add(parameterizedInstance);
		return this;
	}

	@Override
	public final T[] create(String symbol, Parameters params) {
		if (providers.isEmpty()) {
			return empty;
		}
		T[] out = Arrays.copyOf(empty, providers.size());
		for (int i = 0; i < out.length; i++) {
			out[i] = providers.get(i).create(symbol, params);
		}
		return out;
	}

	public static <T> T[] getInstances(String symbol, Parameters params, InstancesProvider<T> provider, String description, boolean mandatory) {
		T[] instancesToUse = provider.create(symbol, params);
		if (ArrayUtils.isEmpty(instancesToUse) && mandatory) {
			throw new IllegalStateException("Can't execute market simulation. No " + description + " provided for symbol " + symbol);
		}
		for (T instance : instancesToUse) {
			if (allInstances.contains(instance)) {
				throw new IllegalStateException("Can't execute market simulation. " + description + " instance provided for symbol " + symbol + " is already in use. Make sure to build a *new* " + description + " object for each symbol.");
			} else {
				allInstances.add(instance);
			}
		}
		return instancesToUse;
	}

	public void clear(){
		providers.clear();
	}
}
