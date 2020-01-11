package com.univocity.trader.chart;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.ruler.*;
import com.univocity.trader.chart.charts.scrolling.*;

import javax.swing.*;
import java.awt.*;

public class ChartWindow extends JFrame {

	private CandleHistory candleHistory;
	private TimeIntervalSelector timeIntervalSelector;
	private BasicChart chart;
	private JButton addCandleButton;
	private JPanel centralPanel;
	private JPanel leftPanel;
	private CandleHistoryView chartHistoryView;
	private ValueRuler valueRuler;
	private TimeRuler timeRuler;


	public ChartWindow() {
		this.setLayout(new BorderLayout());
		this.setTitle("No candle data loaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, 800, 600);
		this.setLocationRelativeTo(null);

		this.add(getCentralPanel(), BorderLayout.CENTER);
		this.add(new JScrollPane(getLeftPanel()), BorderLayout.WEST);

		this.add(getAddCandleButton(), BorderLayout.SOUTH);

		addCandles();
	}

	private JPanel getLeftPanel() {
		if (leftPanel == null) {
			leftPanel = new JPanel();
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
			leftPanel.add(getChart().getController().getControlPanel());
			leftPanel.add(getValueRuler().getController().getControlPanel());
			leftPanel.add(getTimeRuler().getController().getControlPanel());
		}
		return leftPanel;
	}

	private TimeRuler getTimeRuler() {
		if (timeRuler == null) {
			timeRuler = new TimeRuler(getChart());
		}
		return timeRuler;
	}

	protected CandleHistory getCandleHistory() {
		if (candleHistory == null) {
			candleHistory = new CandleHistory();
		}
		return candleHistory;
	}

	private JPanel getCentralPanel() {
		if (centralPanel == null) {
			centralPanel = new JPanel(new BorderLayout());
			centralPanel.add(getChart(), BorderLayout.CENTER);
			centralPanel.add(getTimeIntervalSelector(), BorderLayout.SOUTH);
		}
		return centralPanel;
	}

	private ValueRuler getValueRuler() {
		if (valueRuler == null) {
			valueRuler = new ValueRuler(getChart());

		}
		return valueRuler;
	}

	private JButton getAddCandleButton() {
		if (addCandleButton == null) {
			addCandleButton = new JButton("Add candle");
			addCandleButton.addActionListener(e -> {
				addCandle(getCandleHistory().size());
				getCandleHistory().notifyUpdateListeners();
			});
		}
		return addCandleButton;
	}

	private CandleHistoryView getChartHistoryView() {
		if (chartHistoryView == null) {
			chartHistoryView = getCandleHistory().newView();
		}
		return chartHistoryView;
	}

	private BasicChart getChart() {
		if (chart == null) {
			chart = new CandleChart(getChartHistoryView());
			chart.enableScrolling();
			getCandleHistory().addDataUpdateListener(() -> getTimeIntervalSelector().dataUpdated());

			getTimeIntervalSelector().addIntervalListener(chartHistoryView::updateView);
		}
		return chart;
	}

	private TimeIntervalSelector getTimeIntervalSelector() {
		if (timeIntervalSelector == null) {
			LineChart selectorChart = new LineChart(getCandleHistory().newView());

			timeIntervalSelector = new TimeIntervalSelector(getCandleHistory(), selectorChart);
		}
		return timeIntervalSelector;
	}

	private void addCandle(int i) {
		int size = getCandleHistory().size();

		if (i >= values.length) {
			i = (i % values.length);
		}

		getCandleHistory().addSilently(new Candle(size * 60000 * 60, (size + 1) * 60000 * 60, values[i][0], values[i][2], values[i][3], values[i][1], values[i][4]));
	}

	private void addCandles() {
		for (int x = 0; x < 20; x++) {
			for (int i = 0; i < values.length; i++) {
				addCandle(i);
			}
		}
		getCandleHistory().notifyUpdateListeners();
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
		SwingUtilities.invokeLater(() -> new ChartWindow().setVisible(true));
	}
}
