package com.univocity.trader.chart.annotation;

import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.dynamic.*;

import java.lang.annotation.*;

@Repeatable(Render.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Render {
	String value () default "getValue";
	String description () default "";
	Class<? extends Renderer<?>> renderer () default LineRenderer.class;
	Class<? extends Theme> theme () default Theme.class;

	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.METHOD})
	@interface List {
		Render[] value();
	}
}
