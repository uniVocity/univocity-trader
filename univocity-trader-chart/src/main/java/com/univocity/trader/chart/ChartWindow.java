package com.univocity.trader.chart;

import com.github.weisj.darklaf.*;
import com.github.weisj.darklaf.theme.*;
import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.charts.painter.Painter;
import com.univocity.trader.chart.charts.ruler.*;
import com.univocity.trader.chart.charts.scrolling.*;
import com.univocity.trader.chart.indicators.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
	private IndicatorSelector indicatorSelector;

	private JPopupMenu popup;
	private JMenuItem addIndicatorMenuItem;


	public ChartWindow() {
		this.setLayout(new BorderLayout());
		this.setTitle("No candle data loaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, 1000, 800);
		this.setLocationRelativeTo(null);

		this.add(getCentralPanel(), BorderLayout.CENTER);
		this.add(new JScrollPane(getLeftPanel()), BorderLayout.WEST);
		this.add(getTopPanel(), BorderLayout.NORTH);
	}

	private JPopupMenu getPopup() {
		if (popup == null) {
			popup = new JPopupMenu();
			popup.add(getAddIndicatorMenuItem());
		}
		return popup;
	}

	private JMenuItem getAddIndicatorMenuItem() {
		if (addIndicatorMenuItem == null) {
			addIndicatorMenuItem = new JMenuItem("Indicators...");
			addIndicatorMenuItem.setMnemonic(KeyEvent.VK_I);
			addIndicatorMenuItem.addActionListener((e) -> getIndicatorSelector().getDialog().setVisible(true));
		}
		return addIndicatorMenuItem;
	}

	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel(new GridLayout(1, 2));
			topPanel.add(getSymbolSelector());
//			topPanel.add(getIndicatorSelector());
		}
		return topPanel;
	}

	private SymbolSelector getSymbolSelector() {
		if (symbolSelector == null) {
			symbolSelector = new SymbolSelector(getCandleHistory());
			setGlassPane(symbolSelector.getGlassPane());
			symbolSelector.addLiveFeedConsumer((c) -> getValueRuler().setCurrentPrice(c == null ? 0.0 : c.close));
		}
		return symbolSelector;
	}

	private JPanel getLeftPanel() {
		if (leftPanel == null) {
			leftPanel = new JPanel();
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
			leftPanel.add(getChart().theme().getThemeSettingsPanel());
			leftPanel.add(getValueRuler().theme().getThemeSettingsPanel());
			leftPanel.add(getTimeRuler().theme().getThemeSettingsPanel());
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

	private IndicatorSelector getIndicatorSelector() {
		if (indicatorSelector == null) {
			indicatorSelector = new IndicatorSelector(() -> getSymbolSelector().getInterval());
			indicatorSelector.addIndicatorListener(this::indicatorUpdated);
			indicatorSelector.loadIndicatorsFrom(DefaultIndicators.class);
		}
		return indicatorSelector;
	}

	private void indicatorUpdated(boolean preview, VisualIndicator previous, VisualIndicator current) {
		Painter.Overlay z = current == null ? Painter.Overlay.FRONT : current.overlay();
		if (preview) {
			getChart().removePainter(previous);
			getChart().addPainter(z, current);
		} else {
			if (current == null) {
				getChart().removePainter(previous);
			} else if (previous == null) {
				getChart().addPainter(z, current);
			} else {
				getChart().removePainter(previous);
				getChart().addPainter(z, current);
			}
		}
	}


	private void updateHistory() {

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
			chart.addPainterSelectedListener(painter -> getIndicatorSelector().displayOptionsFor(painter));

			chart.canvas.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					showPopup(e);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					showPopup(e);
				}

				private void showPopup(MouseEvent e) {
					if (e.isPopupTrigger()) {
						getPopup().show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
		return chart;
	}

	private TimeIntervalSelector getTimeIntervalSelector() {
		if (timeIntervalSelector == null) {
			LineChart selectorChart = new LineChart(getCandleHistory().newView());
			timeIntervalSelector = new TimeIntervalSelector(getCandleHistory(), selectorChart);
			timeIntervalSelector.addIntervalListener((f, t) -> getIndicatorSelector().recalculateIndicators());
		}
		return timeIntervalSelector;
	}

	public static void main(String... args) {
		LafManager.install(new DarculaTheme());
		SwingUtilities.invokeLater(() -> new ChartWindow().setVisible(true));
	}
}
