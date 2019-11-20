package com.univocity.trader.utils;

public class LinearRegression {

	private long count;
	private double last;

	private double meanX, meanY, varX, covXY, slope, intercept;
	private double umeanX, umeanY, uvarX, ucovXY;

	public LinearRegression() {
	}

	public void update(double y) {
		this.add(y, true);
	}

	public void accumulate(double y, boolean updating) {
		if (updating) {
			this.update(y);
		} else {
			this.add(y);
		}
	}

	private void add(double y, boolean updating) {
		last = y;

		if (updating && count > 2) {
			double dx = count - meanX;
			double dy = y - meanY;
			double t = ((count - 1.0) / count) * dx;
			uvarX += (t * dx - varX) / count;
			ucovXY += (t * dy - covXY) / count;
			umeanX = dx / count;
			umeanY = dy / count;

			varX += uvarX;
			covXY += ucovXY;
			meanX += umeanX;
			meanY += umeanY;

			slope = covXY / varX;
			intercept = meanY - slope * meanX;
		} else {
			varX -= uvarX;
			covXY -= ucovXY;
			meanX -= umeanX;
			meanY -= umeanY;
		}

	}

	public void add(double y) {
		add(y, false);

		count++;
		double dx = count - meanX;
		double dy = y - meanY;
		double t = ((count - 1.0) / count) * dx;
		varX += (t * dx - varX) / count;
		covXY += (t * dy - covXY) / count;
		meanX += dx / count;
		meanY += dy / count;

		uvarX = 0;
		ucovXY = 0;
		umeanX = 0;
		umeanY = 0;

		slope = covXY / varX;
		intercept = meanY - slope * meanX;
	}

	public double predict(int next) {
		return slope * (count + next) + intercept;
	}

	public double last() {
		return last;
	}

	public boolean goingDown() {
		return goingDown(100.0);
	}

	public boolean goingDown(double factor) {
		return predict(1) < last * (1.0 - (factor / 100.0));
	}

	public boolean goingUp() {
		return goingUp(100.0);
	}

	public boolean goingUp(double factor) {
		return predict(1) > last * (1.0 + (factor / 100.0));
	}
}