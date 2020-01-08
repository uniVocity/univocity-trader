package com.univocity.trader.chart.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface SpinnerBound {
	int minimum() default 1;

	int maximum() default -1;

	int increment() default 1;
}
