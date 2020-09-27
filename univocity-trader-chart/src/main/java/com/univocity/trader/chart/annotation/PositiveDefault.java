package com.univocity.trader.chart.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Inherited
public @interface PositiveDefault {
	double value() default 1.0;

	double maximum() default Integer.MAX_VALUE;

	double increment() default 1.0;
}
