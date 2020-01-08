package com.univocity.trader.chart.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface CheckBoxBound {
	String value();
}
