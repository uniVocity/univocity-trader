package com.univocity.trader.chart;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;

import javax.swing.*;
import java.awt.*;

public class ChartWindow extends JFrame {

	private TimeIntervalSelector timeIntervalSelector;
	private CandleChart chart;
	private JButton addCandleButton;
	private JPanel centralPanel;

	public ChartWindow() {
		this.setLayout(new BorderLayout());
		this.setTitle("No candle data loaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, 800, 600);
		this.setLocationRelativeTo(null);

		this.add(getCentralPanel(), BorderLayout.CENTER);
		this.add(new JScrollPane(getChart().getController().getControlPanel()), BorderLayout.WEST);

		this.add(getAddCandleButton(), BorderLayout.SOUTH);

		addCandles();
	}

	private JPanel getCentralPanel() {
		if (centralPanel == null) {
			centralPanel = new JPanel(new BorderLayout());
			centralPanel.add(new JScrollPane(getChart()), BorderLayout.CENTER);
			centralPanel.add(getTimeIntervalSelector(), BorderLayout.SOUTH);
		}
		return centralPanel;
	}

	private JButton getAddCandleButton() {
		if (addCandleButton == null) {
			addCandleButton = new JButton("Add candle");
			addCandleButton.addActionListener(e -> {
				addCandle(getChart().tradeHistory.size());
				getChart().dataUpdated();
			});
		}
		return addCandleButton;
	}

	private CandleChart getChart() {
		if (chart == null) {
			chart = new CandleChart();
			chart.addDataUpdateListener(() -> getTimeIntervalSelector().dataUpdated());
		}
		return chart;
	}

	private TimeIntervalSelector getTimeIntervalSelector() {
		if (timeIntervalSelector == null) {
			LineChart selectorChart = new LineChart();
			selectorChart.tradeHistory = getChart().tradeHistory;

			getChart().addDataUpdateListener(selectorChart::dataUpdated);

			timeIntervalSelector = new TimeIntervalSelector(selectorChart);

			getChart().addDataUpdateListener(timeIntervalSelector::repaint);
		}
		return timeIntervalSelector;
	}

	private void addCandle(int i) {
		int size = getChart().tradeHistory.size();

		if (i >= values.length) {
			i = (i % values.length);
		}

		getChart().tradeHistory.add(new Candle(size * 60000, (size + 1) * 60000, values[i][0], values[i][2], values[i][3], values[i][1], values[i][4]));
	}

	private void addCandles() {
		for (int i = 0; i < values.length; i++) {
			addCandle(i);
		}
		getChart().dataUpdated();
	}

	double[][] values = new double[][]{
			{44.98, 45.05, 45.17, 44.96, 1},
			{45.05, 45.10, 45.15, 44.99, 2},
			{45.11, 45.19, 45.32, 45.11, 1},
			{45.19, 45.14, 45.25, 45.04, 3},
			{45.12, 45.15, 45.20, 45.10, 1},
			{45.15, 45.14, 45.20, 45.10, 2},
			{45.13, 45.10, 45.16, 45.07, 1},
			{45.12, 45.15, 45.22, 45.10, 5},
			{45.15, 45.22, 45.27, 45.14, 1},
			{45.24, 45.43, 45.45, 45.20, 1},
			{45.43, 45.44, 45.50, 45.39, 1},
			{45.43, 45.55, 45.60, 45.35, 5},
			{45.58, 45.55, 45.61, 45.39, 7},
			{45.45, 45.01, 45.55, 44.80, 6},
			{45.03, 44.23, 45.04, 44.17, 1},
			{44.23, 43.95, 44.29, 43.81, 2},
			{43.91, 43.08, 43.99, 43.08, 1},
			{43.07, 43.55, 43.65, 43.06, 7},
			{43.56, 43.95, 43.99, 43.53, 6},
			{43.93, 44.47, 44.58, 43.93, 1},
	};

	public static void main(String... args) {
		new ChartWindow().setVisible(true);
	}
}
