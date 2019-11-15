package com.univocity.trader.utils;

import com.univocity.trader.simulation.*;
import org.apache.commons.lang3.*;

import java.util.*;

public class Instances<T> extends NewInstances<T> {

	public Instances(T[] empty) {
		super(empty);
	}

	public Instances<T> add(T reusableInstance) {
		add((s, p) -> reusableInstance);
		return this;
	}
}
