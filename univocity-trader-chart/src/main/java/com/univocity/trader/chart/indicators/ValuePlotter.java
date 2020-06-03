package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.controls.*;
import com.univocity.trader.chart.charts.painter.*;

import java.awt.*;
import java.util.function.*;

public class ValuePlotter implements Repaintable {

	private double[] values;
	private int i = 0;
	private final DoubleSupplier valueSupplier;
	private Point previousLocation;
	private final LinePlotController<ValuePlotter> controller = new LinePlotController<>(this);
	private final Repaintable parent;

	public ValuePlotter(Repaintable parent, DoubleSupplier valueSupplier) {
		this.valueSupplier = valueSupplier;
		this.parent = parent;
	}

	public void reset(int length) {
		values = new double[length];
		i = 0;
	}

	public void nextValue() {
		values[i++] = valueSupplier.getAsDouble();
	}

	protected Point createCoordinate(BasicChart<?> chart, int candleIndex, double value) {
		Point p = new Point();
		p.x = chart.getXCoordinate(candleIndex);
		p.y = chart.getYCoordinate(value);
		return p;
	}

	public void plotNext(int i, BasicChart<?> chart, Graphics2D g, int width) {
		g.setStroke(controller.getNormalStroke());
		g.setColor(controller.getLineColor());

		Point location = createCoordinate(chart, i, values[i]);

		if (i == 0) {
			previousLocation = location;
		}

		plot(previousLocation, location, i, chart, g, width);

		previousLocation = location;
	}

	protected void plot(Point previousLocation, Point currentLocation, int i, BasicChart<?> chart, Graphics2D g, int width) {
		g.drawLine(previousLocation.x, previousLocation.y, currentLocation.x, currentLocation.y);
	}

	@Override
	public void invokeRepaint() {
		parent.invokeRepaint();
	}
}
