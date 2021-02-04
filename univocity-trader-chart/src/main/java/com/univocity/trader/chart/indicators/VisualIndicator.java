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

import static com.univocity.trader.chart.charts.painter.Painter.Overlay.*;

public class VisualIndicator extends AreaPainter {

	private static final LineRenderer[] EMPTY = new LineRenderer[0];
	final ArgumentValue[] argumentValues;

	private Aggregator[] aggregators;
	private Indicator indicator;
	BasicChart<?> chart;
	private final Supplier<TimeInterval> interval;
	private Painter<CompositeTheme> indicatorPainter;
	private Renderer[] currentRenderers = EMPTY;
	private final Painter.Overlay overlay;
	private final Rectangle bounds;
	final IndicatorDefinition config;
	private int position = -1;
	private Color[] colors = new Color[0];

	private double[] values;
	private Candle last;

	public VisualIndicator(Supplier<TimeInterval> interval, IndicatorDefinition indicator) {
		this(interval, indicator.getArgumentValues(interval), indicator);
	}

	public VisualIndicator(Supplier<TimeInterval> interval, ArgumentValue[] argumentValues, IndicatorDefinition indicator) {
		this.config = indicator;
		this.argumentValues = argumentValues;
		boolean overlay = indicator.overlay;
		this.overlay = overlay ? Overlay.BACK : NONE;
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
		Map<Integer, Renderer<?>> out = new TreeMap<>();

		for (Method m : getIndicator().getClass().getMethods()) {
			Render[] renderConfig = m.getAnnotationsByType(Render.class);
			if (renderConfig.length == 0) {
				renderConfig = config.renders;
			}
			if ((m.getReturnType() == double.class || renderConfig.length > 0) && m.getParameterCount() == 0) {
				if (renderConfig.length > 0) {
					for (int i = 0; i < renderConfig.length; i++) {
						Render r = renderConfig[i];
						if (r.constant() == Double.MIN_VALUE && r.value().equals(m.getName())) {
							out.put(i, createRenderer(m, r));
						}
					}
				} else {
					out.put(-1, createRenderer(m, null));
				}
			}
		}

		if (config.renders != null) {
			for (Render r : config.renders) {
				if (r.constant() != Double.MIN_VALUE) {
					out.put(-1, createRenderer(r.constant(), r));
				}
			}
		}

		out.put(-2, new SignalRenderer("Signal", new AreaTheme(chart), indicator)); //TODO proper theme

		Renderer[] renderers = out.values().toArray(Renderer[]::new);

		int variableCount = 0;
		for (Renderer r : renderers) {
			if (r.displayValue()) {
				variableCount++;
			}
		}

		values = new double[variableCount];
		return renderers;
	}

	private String getDescription(Method m, Render renderConfig) {
		if (m == null) {
			return "";
		}
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

	private Renderer createRenderer(double constant, Render renderConfig) {
		return createRenderer(null, () -> constant, renderConfig);
	}

	private Renderer createRenderer(Method m, Render renderConfig) {
		return createRenderer(m, () -> invoke(m), renderConfig);
	}

	private Renderer createRenderer(Method m, DoubleSupplier supplier, Render renderConfig) {
		try {
			String description = getDescription(m, renderConfig);
			if (renderConfig == null) {
				if (m == null || m.getReturnType() == double.class) {
					var theme = new LineTheme<>(this);
					theme.setDisplayingLogarithmicScale(overlay != NONE);
					return new LineRenderer(description, theme, supplier);
				}
			} else {
				Class<? extends Theme> themeType = renderConfig.theme();
				Class<? extends Theme> constructorThemeType = null;
				Class<? extends Renderer<?>> rendererType = renderConfig.renderer();

				if (rendererType == LineRenderer.class) {
					constructorThemeType = LineTheme.class;
				} else if (rendererType == HistogramRenderer.class || rendererType == ColoredHistogramRenderer.class) {
					constructorThemeType = HistogramTheme.class;
				} else if (rendererType == MarkerRenderer.class) {
					constructorThemeType = MarkerTheme.class;
				} else if (rendererType == AreaRenderer.class) {
					constructorThemeType = AreaTheme.class;
				}
				if (themeType == Theme.class) {
					themeType = constructorThemeType;
					if (themeType == null) {
						throw new IllegalStateException("No theme defined for renderer " + rendererType.getSimpleName() + " defined in " + m);
					}
				}

				if (constructorThemeType == null) {
					constructorThemeType = themeType;
				}

				Theme theme = themeType.getConstructor(Repaintable.class).newInstance(this);
				theme.setDisplayingLogarithmicScale(overlay != NONE);

				Renderer out;
				String[] args = renderConfig.args();
				List<Class<?>> argumentTypes = new ArrayList<>(Arrays.asList(String.class, constructorThemeType, DoubleSupplier.class));
				List<Object> argumentValues = new ArrayList<>(Arrays.asList(description, theme, supplier));
				if (!"".equals(args[0]) || args.length > 1) {
					for (int i = 0; i < args.length; i++) {
						argumentTypes.add(Supplier.class);
						Method method = getIndicator().getClass().getMethod(args[i]);
						argumentValues.add(objectSupplier(method));
					}
				}

				out = rendererType.getConstructor(argumentTypes.toArray(Class[]::new)).newInstance(argumentValues.toArray());
				if ((m == null || !renderConfig.displayValue()) && out instanceof AbstractRenderer) {
					((AbstractRenderer<?>) out).displayValue(false);
				}
				return out;
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error initializing visualization for indicator returned by " + m, e);
		}
		throw new IllegalStateException("Unable to to initialize visualization for indicator returned by " + m);

	}

	private Supplier<Object> objectSupplier(Method m) {
		return () -> {
			try {
				return m.invoke(getIndicator());
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		};
	}

	private double invoke(Method m) {
		try {
			return (double) m.invoke(getIndicator());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void reset() {
		if (this.aggregators == null) {
			Aggregator aggregator = new Aggregator("").getInstance(interval.get());
			getIndicator().initialize(aggregator);

			aggregators = aggregator.getAggregators();
		} else {
			if (getIndicator().getAccumulationCount() == 0) {
				return;
			}
			BasicChart<?> chart = this.chart;
			chart.removePainter(this);

			this.indicator = null;
			Aggregator aggregator = new Aggregator("").getInstance(interval.get());
			getIndicator().initialize(aggregator);
			aggregators = aggregator.getAggregators();
			indicatorPainter = null;

			chart.addPainter(this.overlay(), this);
		}

		last = chart.candleHistory.getLast();
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void position(int position) {
		this.position = position;
	}

	private Painter<CompositeTheme> getIndicatorPainter() {
		if (indicatorPainter == null) {
			Renderer[] renderers = createRenderers();

			if (config.compose != null) {
				AreaTheme theme = new AreaTheme(chart);
				theme.setDisplayingLogarithmicScale(overlay != NONE);
				AreaRenderer a = new AreaRenderer(config.compose.description(), theme, renderers);
				currentRenderers = new Renderer[]{a};
			} else {
				currentRenderers = renderers;
			}
			indicatorPainter = new CompositePainter(indicator.getClass().getSimpleName(), this, this::reset, this::process, currentRenderers);
			colors = new Color[currentRenderers.length];
		}
		return indicatorPainter;
	}

	private void process(Candle candle) {
		if (candle == last) {
			//FIXME: ugly workaround to render indicator state based on last candle from history (not fully populated yet).
			//       won't work when live trading.
			for (int i = 0; i < aggregators.length; i++) {
				aggregators[i].setFull(candle);
			}
		} else {
			for (int i = 0; i < aggregators.length; i++) {
				aggregators[i].aggregate(candle);
			}
		}

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
	public void paintOn(BasicChart<?> chart, Graphics2D g, int width, Overlay overlay) {
		getIndicatorPainter().paintOn(chart, g, width, overlay);
	}

	@Override
	public void install(BasicChart<?> chart) {
		this.chart = chart;
		getIndicatorPainter().install(chart);
	}

	@Override
	public void uninstall(BasicChart<?> chart) {
		this.chart = null;
		getIndicatorPainter().uninstall(chart);
	}

	@Override
	public void invokeRepaint() {
		getIndicatorPainter().invokeRepaint();
	}

	private Indicator getIndicator() {
		if (indicator == null) {
			indicator = config.create(argumentValues);
		}
		return indicator;
	}

	@Override
	protected void updateMinAndMax(boolean logScale, int from, int to) {
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
				minimum = Math.min(minimum, currentRenderers[i].getMinimumValue(logScale, from, to));
			}
		}
	}

	@Override
	public final double maximumValue(int from, int to) {
		if (config.max != Double.MAX_VALUE) {
			maximum = config.max;
		} else {
			maximum = Integer.MIN_VALUE;
			for (int i = 0; i < currentRenderers.length; i++) {
				maximum = Math.max(maximum, currentRenderers[i].getMaximumValue(from, to));
			}
		}
		return maximum;
	}

	@Override
	public final double minimumValue(boolean logScale, int from, int to) {
		if (config.min != Double.MIN_VALUE) {
			minimum = config.min;
		} else {
			logScale &= theme().isDisplayingLogarithmicScale();
			minimum = Integer.MAX_VALUE;
			for (int i = 0; i < currentRenderers.length; i++) {
				minimum = Math.min(minimum, currentRenderers[i].getMinimumValue(logScale, from, to));
			}
		}
		return minimum;
	}

	public Color[] getCurrentSelectionColors() {
		return colors;
	}

	public double[] getCurrentSelectionValues(int position) {
		if (position < 0) {
			return null;
		}
		for (int i = 0, c = 0; i < currentRenderers.length; i++) {
			Renderer<?> r = currentRenderers[i];
			if (r.displayValue()) {
				values[c] = r.getValueAt(position);
				colors[c] = r.getColorAt(position);
				c++;
			}
		}
		return values;
	}

	void updateEditorValues() {
		config.setEditorValues(argumentValues);
	}
}
