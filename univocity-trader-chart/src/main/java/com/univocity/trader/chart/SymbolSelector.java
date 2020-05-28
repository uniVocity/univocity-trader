package com.univocity.trader.chart;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.gui.*;
import com.univocity.trader.chart.gui.time.*;
import com.univocity.trader.config.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.util.List;
import java.util.*;

public class SymbolSelector extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(SymbolSelector.class);

	private JComboBox<String> cmbSymbols;
	private DefaultComboBoxModel<String> cmbSymbolsModel;

	private final CandleRepository candleRepository;
	private final CandleHistory candleHistory;

	private DateEditPanel chartStart;
	private DateEditPanel chartEnd;
	private JButton btLoad;

	public SymbolSelector(CandleRepository candleRepository, CandleHistory candleHistory) {
		this.candleRepository = candleRepository;
		this.candleHistory = candleHistory;

		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		this.add(getChartStart(), c);

		c.gridy = 1;
		this.add(getChartEnd(), c);

		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 5, 5, 5);
		this.add(getCmbSymbols(), c);

		c.gridy = 1;
		this.add(getBtLoad(), c);
	}

	private JButton getBtLoad(){
		if(btLoad == null){
			btLoad = new JButton("Load");
			btLoad.setEnabled(false);
			btLoad.addActionListener((e) -> SwingUtilities.invokeLater(this::loadCandles));
		}
		return btLoad;
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
			List<Candle> candles = Collections.list(candleRepository.iterate(symbol, getChartStart().getCommittedValue(), getChartEnd().getCommittedValue(), true));
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
			getChartStart().addDateEditPanelListener(e -> chartEnd.setMinimumValue(e.getNewDate()));
		}
		return chartEnd;
	}

	private void fillAvailableDates(){
		String symbol = validateSymbol();
		setDateSelectionEnabled(symbol != null);
		if(symbol != null) {
			Candle first = candleRepository.firstCandle(symbol);
			Candle last = candleRepository.lastCandle(symbol);

			getChartStart().setMinimumValue(first.openTime);
			getChartEnd().setMaximumValue(last.closeTime);

			getChartStart().setValue(first.openTime);
			getChartEnd().setValue(last.closeTime);
		}
	}

	private void setDateSelectionEnabled(boolean enabled){
		getChartStart().setEnabled(enabled);
		getChartEnd().setEnabled(enabled);
	}

	public static void main(String... args) {
		DatabaseConfiguration databaseConfiguration = new SimulationConfiguration().database();
		CandleRepository candleRepository = new CandleRepository(databaseConfiguration);

		SymbolSelector symbolSelector = new SymbolSelector(candleRepository, new CandleHistory());
		WindowUtils.displayTestFrame(symbolSelector);
	}
}
