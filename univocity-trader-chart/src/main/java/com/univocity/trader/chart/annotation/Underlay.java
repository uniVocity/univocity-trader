package com.univocity.trader.chart.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Underlay {
	String label() default "";
	double min() default Double.MIN_VALUE;
	double max() default Double.MAX_VALUE;
}
