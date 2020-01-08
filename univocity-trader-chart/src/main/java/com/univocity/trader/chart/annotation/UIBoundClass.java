package com.univocity.trader.chart.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface UIBoundClass {
	Class<? extends UpdateProcessor> updateProcessor();
}
