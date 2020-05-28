package com.univocity.trader.chart;

import com.github.weisj.darklaf.*;
import com.github.weisj.darklaf.theme.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.ruler.*;
import com.univocity.trader.chart.charts.scrolling.*;
import com.univocity.trader.chart.gui.time.*;
import com.univocity.trader.config.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;

public class ChartWindow extends JFrame {

	private CandleHistory candleHistory;
	private TimeIntervalSelector timeIntervalSelector;
	private BasicChart<?> chart;
	private JPanel centralPanel;
	private JPanel leftPanel;
	private JPanel topPanel;
	private CandleHistoryView chartHistoryView;
	private ValueRuler valueRuler;
	private TimeRuler timeRuler;
	private SymbolSelector symbolSelector;


	public ChartWindow() {
		this.setLayout(new BorderLayout());
		this.setTitle("No candle data loaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, 800, 600);
		this.setLocationRelativeTo(null);

		this.add(getCentralPanel(), BorderLayout.CENTER);
		this.add(new JScrollPane(getLeftPanel()), BorderLayout.WEST);
		this.add(getTopPanel(), BorderLayout.NORTH);
	}

	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			topPanel.add(getSymbolSelector());
		}
		return topPanel;
	}

	private SymbolSelector getSymbolSelector() {
		if (symbolSelector == null) {
			DatabaseConfiguration databaseConfiguration = new SimulationConfiguration().database();
			CandleRepository candleRepository = new CandleRepository(databaseConfiguration);

			symbolSelector = new SymbolSelector(candleRepository, getCandleHistory());
		}
		return symbolSelector;
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
			centralPanel.add(getChart().canvas, BorderLayout.CENTER);
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

	private void updateHistory(){

	}

//	private JButton getAddCandleButton() {
//		if (addCandleButton == null) {
//			addCandleButton = new JButton("Add candle");
//			addCandleButton.addActionListener(e -> {
//				addCandle(getCandleHistory().size());
//				getCandleHistory().notifyUpdateListeners();
//			});
//		}
//		return addCandleButton;
//	}

	private CandleHistoryView getChartHistoryView() {
		if (chartHistoryView == null) {
			chartHistoryView = getCandleHistory().newView();
		}
		return chartHistoryView;
	}

	private BasicChart<?> getChart() {
		if (chart == null) {
			chart = new CandleChart(getChartHistoryView());
			chart.canvas.enableScrolling();
			getCandleHistory().addDataUpdateListener((type) -> getTimeIntervalSelector().dataUpdated(type));

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

	public static void main(String... args) {
		LafManager.install(new DarculaTheme());
		SwingUtilities.invokeLater(() -> new ChartWindow().setVisible(true));
	}
}
