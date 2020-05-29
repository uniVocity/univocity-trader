package com.univocity.trader.chart;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.gui.*;
import com.univocity.trader.chart.gui.components.*;
import com.univocity.trader.chart.gui.components.time.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.util.List;
import java.util.*;

public class SymbolSelector extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(SymbolSelector.class);

	private static final int[] YYYY_MM_DD_FIELDS = Arrays.copyOfRange(DateEditPanel.ALL_FIELDS, 0, 3);
	private static final int[] YYYY_MM_DD_HH_FIELDS = Arrays.copyOfRange(DateEditPanel.ALL_FIELDS, 0, 4);
	private static final int[] YYYY_MM_DD_HH_MM_FIELDS = Arrays.copyOfRange(DateEditPanel.ALL_FIELDS, 0, 5);
	private static final int[] YYYY_MM_FIELDS = Arrays.copyOfRange(DateEditPanel.ALL_FIELDS, 0, 2);
	private JComboBox<TimeIntervalType> cmbUnitType;
	private JSpinner txtUnits;

	private JComboBox<String> cmbSymbols;
	private DefaultComboBoxModel<String> cmbSymbolsModel;

	private final CandleRepository candleRepository;
	private final CandleHistory candleHistory;

	private DisabledGlassPane glassPane;
	private DateEditPanel chartStart;
	private DateEditPanel chartEnd;
	private JButton btLoad;

	public SymbolSelector(CandleRepository candleRepository, CandleHistory candleHistory) {
		this.candleRepository = candleRepository;
		this.candleHistory = candleHistory;

		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.gridy = 0;
		c.gridx = 0;
		this.add(getCmbSymbols(), c);

		c.gridx = 1;
		this.add(getTxtUnits(), c);

		c.gridx = 2;
		this.add(getCmbUnitType(), c);

		c.gridx = 3;
		this.add(getBtLoad(), c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 4;
		this.add(getChartStart(), c);

		c.gridy = 2;
		this.add(getChartEnd(), c);
	}

	private JButton getBtLoad() {
		if (btLoad == null) {
			btLoad = new JButton("Load");
			btLoad.setEnabled(false);
			btLoad.addActionListener((e) -> SwingUtilities.invokeLater(this::executeLoadCandles));
		}
		return btLoad;
	}

	private void executeLoadCandles() {
		Thread thread = new Thread(() -> {
			try {
				glassPane.activate("Loading " + getSymbol() + " candles...");
				loadCandles();
			} finally {
				glassPane.deactivate();
			}
		});
		thread.start();
	}

	public String getSymbol() {
		String symbol = (String) cmbSymbols.getSelectedItem();
		if (cmbSymbols.getSelectedIndex() == -1) {
			if (symbol != null) {
				symbol = symbol.trim();
			}
		}

		if (symbol != null && !symbol.isEmpty()) {
			return symbol;
		}
		return null;
	}

	private void updateSymbols() {
		String symbol = getSymbol();
		if (symbol != null) {
			int symbolCount = cmbSymbolsModel.getSize();
			for (int i = 0; i < symbolCount; i++) {
				if (cmbSymbolsModel.getElementAt(i).equals(symbol)) {
					return;
				}
			}
			cmbSymbolsModel.addElement(symbol);
		}
	}

	public void setSelectedSymbol(String symbol) {
		String current = getSymbol();
		if (current == null || !current.equals(symbol)) {
			this.cmbSymbols.setSelectedItem(symbol);
		}
	}

	private JComboBox<String> getCmbSymbols() {
		if (cmbSymbols == null) {
			cmbSymbolsModel = new DefaultComboBoxModel<>(candleRepository.getKnownSymbols().toArray(new String[0]));
			cmbSymbols = new JComboBox<>(cmbSymbolsModel);
			cmbSymbols.setEditable(true);
			cmbSymbols.setSelectedIndex(-1);
			cmbSymbols.addActionListener((e) -> btLoad.setEnabled(true));
			cmbSymbols.addActionListener((e) -> fillAvailableDates());
		}
		return cmbSymbols;
	}

	private void loadCandles() {
		btLoad.setEnabled(false);
		String symbol = validateSymbol();
		if (symbol == null) {
			return;
		}

		try {
			List<Candle> candles = Collections.list(candleRepository.iterate(symbol, getChartStart().getCommittedValue(), getChartEnd().getCommittedValue(), false));
			while (!candles.isEmpty() && candles.get(candles.size() - 1) == null) {
				candles.remove(candles.size() - 1);
			}
			if (candles.size() == 0) {
				WindowUtils.displayWarning(this, "No history data available for symbol " + symbol);
			}
			candleHistory.setCandles(candles);

		} catch (Exception ex) {
			log.error("Error loading data for symbol " + symbol, ex);
			WindowUtils.displayError(this, "Error loading data for symbol " + symbol);
		}
	}

	private String validateSymbol() {
		String symbol = getSymbol();
		if (symbol == null) {
			WindowUtils.displayError(this, "Invalid symbol");
		}

		return symbol;
	}

	private DateEditPanel getChartStart() {
		if (chartStart == null) {
			chartStart = new DateEditPanel(LocalDateTime.now().minusDays(30));
			chartStart.setBorder(new TitledBorder("From"));
			chartStart.addDateEditPanelListener(e -> getBtLoad().setEnabled(true));
			chartStart.setEnabled(false);
			chartStart.setInferLeastPossibleValue(true);
			getChartEnd().addDateEditPanelListener(e -> chartStart.setMaximumValue(e.getNewDate()));
		}
		return chartStart;
	}

	private DateEditPanel getChartEnd() {
		if (chartEnd == null) {
			chartEnd = new DateEditPanel(LocalDateTime.now());
			chartEnd.setBorder(new TitledBorder("To"));
			chartEnd.addDateEditPanelListener(e -> getBtLoad().setEnabled(true));
			chartEnd.setEnabled(false);
			chartEnd.setInferLeastPossibleValue(false);
			getChartStart().addDateEditPanelListener(e -> chartEnd.setMinimumValue(e.getNewDate()));
		}
		return chartEnd;
	}

	private void fillAvailableDates() {
		String symbol = validateSymbol();
		setDateSelectionEnabled(symbol != null);
		if (symbol != null) {
			Candle first = candleRepository.firstCandle(symbol);
			Candle last = candleRepository.lastCandle(symbol);

			getChartStart().setMinimumValue(first.openTime);
			getChartEnd().setMaximumValue(last.closeTime);

			getChartStart().setValue(first.openTime);
			getChartEnd().setValue(last.closeTime);
		}
	}

	private void setDateSelectionEnabled(boolean enabled) {
		getChartStart().setEnabled(enabled);
		getChartEnd().setEnabled(enabled);
	}

	public DisabledGlassPane getGlassPane() {
		if (glassPane == null) {
			glassPane = new DisabledGlassPane();
			glassPane.setBackground(new Color(125, 125, 125, 125));
			glassPane.setForeground(Color.BLUE);
		}
		return glassPane;
	}

	private void changeDateFormats() {
		TimeIntervalType timeInterval = (TimeIntervalType) this.cmbUnitType.getSelectedItem();
		try {
			getChartStart().setEnabled(false, DateEditPanel.ALL_FIELDS);
			getChartEnd().setEnabled(false, DateEditPanel.ALL_FIELDS);
			getChartStart().setEnabled(true, getEnabledFields(timeInterval));
			getChartEnd().setEnabled(true, getEnabledFields(timeInterval));
		} catch (Exception e) {
			log.error("error", e);
		}
	}

	private JComboBox getCmbUnitType() {
		if (cmbUnitType == null) {
			cmbUnitType = new JComboBox<>();
			cmbUnitType.setModel(new DefaultComboBoxModel<>(TimeIntervalType.values()));
			cmbUnitType.setSelectedItem(TimeIntervalType.DAY);
			cmbUnitType.addItemListener(evt -> changeDateFormats());
		}
		return cmbUnitType;
	}

	private int[] getEnabledFields(TimeIntervalType timeInterval) {
		switch (timeInterval) {
			case MONTH:
				return YYYY_MM_FIELDS;
			case HOUR:
				return YYYY_MM_DD_HH_FIELDS;
			case MINUTE:
				return YYYY_MM_DD_HH_MM_FIELDS;
			case SECOND:
				return DateEditPanel.ALL_FIELDS;
			default:
				return YYYY_MM_DD_FIELDS;
		}
	}

	private JSpinner getTxtUnits() {
		if (txtUnits == null) {
			txtUnits = new JSpinner();
			SpinnerNumberWrapModel model = new SpinnerNumberWrapModel(txtUnits, 1, 1, 9999, 1);
			txtUnits.setModel(model);
		}
		return txtUnits;
	}

	public TimeInterval getInterval() {
		Integer units = (Integer) txtUnits.getValue();
		TimeIntervalType type = (TimeIntervalType) cmbUnitType.getSelectedItem();
		return type.toTimeInterval(units);
	}


	public static void main(String... args) {
		DatabaseConfiguration databaseConfiguration = new SimulationConfiguration().database();
		CandleRepository candleRepository = new CandleRepository(databaseConfiguration);

		SymbolSelector symbolSelector = new SymbolSelector(candleRepository, new CandleHistory());
		WindowUtils.displayTestFrame(symbolSelector);
	}
}
