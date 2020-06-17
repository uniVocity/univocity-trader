package com.univocity.trader.chart.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.charts.theme.*;
import com.univocity.trader.chart.dynamic.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import org.apache.commons.lang3.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

public class VisualIndicator extends AreaPainter {

	private static final LineRenderer[] EMPTY = new LineRenderer[0];

	private Aggregator aggregator;
	private Indicator indicator;
	BasicChart<?> chart;
	private final Supplier<TimeInterval> interval;
	private Painter<CompositeTheme> indicatorPainter;
	private Renderer[] currentRenderers = EMPTY;
	private final Painter.Overlay overlay;
	private final Rectangle bounds;
	private final IndicatorDefinition config;

	private double[] values;

	public VisualIndicator(Supplier<TimeInterval> interval, IndicatorDefinition indicator) {
		this.config = indicator;
		boolean overlay = indicator.overlay;
		this.overlay = overlay ? Overlay.FRONT : Overlay.NONE;
		this.bounds = overlay ? null : new Rectangle(0, 0, 0, 0);
		this.interval = interval;
		if (!overlay) {
			theme().setDisplayingLogarithmicScale(false);
		}
	}

	@Override
	public String header() {
		return indicatorPainter == null ? null : indicatorPainter.header();
	}

	private Renderer[] createRenderers() {
		List<Renderer<?>> out = new ArrayList<>();

		for (Method m : getIndicator().getClass().getMethods()) {
			Render[] renderConfig = m.getAnnotationsByType(Render.class);
			if (renderConfig.length == 0) {
				renderConfig = config.renders;
			}
			if ((m.getReturnType() == double.class || renderConfig.length > 0) && m.getParameterCount() == 0) {
				if (renderConfig.length > 0) {
					for (Render r : renderConfig) {
						if (r.value().equals(m.getName())) {
							out.add(createRenderer(m, r));
						}
					}
				} else {
					out.add(createRenderer(m, null));
				}
			}
		}

		Renderer[] renderers = out.toArray(Renderer[]::new);

		int variableCount = 0;
		for (Renderer r : renderers) {
			if (!r.constant()) {
				variableCount++;
			}
		}

		values = new double[variableCount];
		return renderers;
	}

	private String getDescription(Method m, Render renderConfig) {
		String description = renderConfig == null ? "" : renderConfig.description();

		if (description.isBlank()) {
			description = m.getName();
			if (description.equals("getValue")) {
				description = "";
			}
		}

		if (description.startsWith("get")) {
			description = description.substring(3);
		}
		if (!description.isBlank()) {
			description = StringUtils.capitalize(description);
		}
		return description;
	}

	private Renderer createRenderer(Method m, Render renderConfig) {
		try {
			String description = getDescription(m, renderConfig);
			if (renderConfig == null) {
				if (m.getReturnType() == double.class) {
					return new LineRenderer(description, new LineTheme<>(this), () -> invoke(m));
				}
			} else {
				Class<? extends Theme> themeType = renderConfig.theme();
				Class<? extends Renderer<?>> rendererType = renderConfig.renderer();
				if (themeType == Theme.class) { //try to guess from existing implementations.
					if (rendererType == LineRenderer.class) {
						themeType = LineTheme.class;
					} else if (rendererType == HistogramRenderer.class) {
						themeType = HistogramTheme.class;
					} else if (rendererType == MarkerRenderer.class) {
						themeType = MarkerTheme.class;
					} else if (rendererType == AreaRenderer.class) {
						themeType = AreaTheme.class;
					} else {
						throw new IllegalStateException("No theme defined for renderer " + rendererType.getSimpleName() + " defined in " + m);
					}
				}


				Theme theme = themeType.getConstructor(Repaintable.class).newInstance(this);

				Renderer out;
				out = rendererType.getConstructor(String.class, theme.getClass(), DoubleSupplier.class).newInstance(description, theme, (DoubleSupplier) () -> invoke(m));
				if (renderConfig.constant() && out instanceof DoubleRenderer) {
					((DoubleRenderer<?>) out).setConstant(renderConfig.constant());
				}
				return out;
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error initializing visualization for indicator returned by " + m, e);
		}
		throw new IllegalStateException("Unable to to initialize visualization for indicator returned by " + m);

	}

	private double invoke(Method m) {
		try {
			return (double) m.invoke(getIndicator());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void reset() {
		aggregator = new Aggregator("").getInstance(interval.get());
		getIndicator().initialize(aggregator);
	}

	private Painter<CompositeTheme> getIndicatorPainter() {
		if (indicatorPainter == null) {
			Renderer[] renderers = createRenderers();

			if (config.compose != null) {
				AreaRenderer a = new AreaRenderer(config.compose.description(), new AreaTheme(chart), renderers);
				currentRenderers = new Renderer[]{a};
			} else {

				currentRenderers = renderers;
			}


			indicatorPainter = new CompositePainter(indicator.getClass().getSimpleName(), this, this::reset, this::process, currentRenderers);
		}
		return indicatorPainter;
	}

	private void process(Candle candle) {
		aggregator.aggregate(candle);
		getIndicator().accumulate(candle);
	}

	@Override
	public Rectangle bounds() {
		return bounds;
	}

	@Override
	public Overlay overlay() {
		return overlay;
	}

	@Override
	public CompositeTheme theme() {
		return getIndicatorPainter().theme();
	}

	@Override
	public void paintOn(BasicChart<?> chart, Graphics2D g, int width) {
		getIndicatorPainter().paintOn(chart, g, width);
	}

	@Override
	public void install(BasicChart<?> chart) {
		getIndicatorPainter().install(chart);
	}

	@Override
	public void uninstall(BasicChart<?> chart) {
		getIndicatorPainter().uninstall(chart);
	}

	@Override
	public void invokeRepaint() {
		getIndicatorPainter().invokeRepaint();
	}

	private Indicator getIndicator() {
		if (indicator == null) {
			indicator = config.create(interval.get());
		}
		return indicator;
	}

	@Override
	protected void updateMinAndMax(int from, int to) {
		if (config.max != Double.MAX_VALUE && config.min != Double.MIN_VALUE) {
			maximum = config.max;
			minimum = config.min;
		} else {
			if (config.max != Double.MAX_VALUE) {
				maximum = config.max;
			}
			if (config.min != Double.MIN_VALUE) {
				minimum = config.min;
			}
			for (int i = 0; i < currentRenderers.length; i++) {
				maximum = Math.max(maximum, currentRenderers[i].getMaximumValue(from, to));
				minimum = Math.min(minimum, currentRenderers[i].getMinimumValue(from, to));
			}
		}
	}

	@Override
	public final double maximumValue(int from, int to) {
		if (config.max != Double.MAX_VALUE) {
			maximum = config.max;
		} else {
			for (int i = 0; i < currentRenderers.length; i++) {
				maximum = Math.max(maximum, currentRenderers[i].getMaximumValue(from, to));
			}
		}
		return maximum;
	}

	@Override
	public final double minimumValue(int from, int to) {
		if (config.min != Double.MIN_VALUE) {
			minimum = config.min;
		} else {
			for (int i = 0; i < currentRenderers.length; i++) {
				minimum = Math.min(minimum, currentRenderers[i].getMinimumValue(from, to));
			}
		}
		return minimum;
	}

	public double[] getCurrentSelectionValues(int position) {
		if (position < 0) {
			return null;
		}
		for (int i = 0, c = 0; i < currentRenderers.length; i++) {
			Renderer<?> r = currentRenderers[i];
			if (!r.constant()) {
				values[c] = r.getValueAt(position);
				c++;
			}
		}
		return values;
	}
}
