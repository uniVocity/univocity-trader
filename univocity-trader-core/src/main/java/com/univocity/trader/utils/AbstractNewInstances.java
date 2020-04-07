package com.univocity.trader.utils;

import com.univocity.trader.simulation.*;
import org.apache.commons.lang3.*;

import java.util.*;
import java.util.function.*;

public abstract class AbstractNewInstances<T, S extends AbstractNewInstances<T, S>> implements InstancesProvider<T>, Cloneable {

	private final T[] empty;
	List<InstanceProvider<T>> providers = new ArrayList<>();

	public AbstractNewInstances(T[] empty) {
		this.empty = empty;
	}

	public final S add(Supplier<T> instanceSupplier) {
		add((s, p) -> instanceSupplier.get());
		return (S) this;
	}

	public final S add(Function<String, T> nonParameterizedInstance) {
		add((s, p) -> nonParameterizedInstance.apply(s));
		return (S) this;
	}

	public final S add(InstanceProvider<T> parameterizedInstance) {
		providers.add(parameterizedInstance);
		return (S) this;
	}

	public final S add(Class<T> instanceType) {
		providers.add(new ReflectiveInstanceProvider<T>(instanceType));
		return (S) this;
	}

	@Override
	public final T[] create(String symbol, Parameters params) {
		if (providers.isEmpty()) {
			return empty;
		}
		Set<T> tmp = new HashSet<>();
		for (int i = 0; i < providers.size(); i++) {
			tmp.add(providers.get(i).create(symbol, params));
		}
		T[] out = Arrays.copyOf(empty, tmp.size());
		return tmp.toArray(out);
	}

	public static <T> T[] getInstances(String symbol, Parameters params, InstancesProvider<T> provider, String description, boolean mandatory, Set<Object> allInstances) {
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

	public final S clear() {
		providers.clear();
		return (S) this;
	}

	@Override
	public S clone() {
		try {
			S out = (S) super.clone();
			out.providers = new ArrayList<>(this.providers);
			return out;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}
}
