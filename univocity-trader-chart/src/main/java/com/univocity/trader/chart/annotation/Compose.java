package com.univocity.trader.chart.annotation;

import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.dynamic.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Compose {

	String description() default "";

	Class<? extends CompositeRenderer<?>> renderer();

	Class<? extends Theme> theme() default Theme.class;

	Render[] elements();
}
