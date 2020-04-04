package com.univocity.trader.chart;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.gui.*;
import com.univocity.trader.config.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.time.temporal.*;
import java.util.List;
import java.util.*;

public class SymbolSelector extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(SymbolSelector.class);

	private JComboBox<String> cmbSymbols;
	private DefaultComboBoxModel<String> cmbSymbolsModel;

	private final CandleRepository candleRepository;
	private final CandleHistory candleHistory;

	public SymbolSelector(CandleRepository candleRepository, CandleHistory candleHistory) {
		this.candleRepository = candleRepository;
		this.candleHistory = candleHistory;

		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 5, 5, 5);
		this.add(new JLabel("Symbol"), c);

		c.gridx = 1;
		c.weightx = 1;
		c.insets = new Insets(5, 0, 5, 5);
		this.add(getCmbSymbols(), c);

		this.setBorder(new TitledBorder("Symbol data"));
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
			cmbSymbols.addActionListener((e) -> SwingUtilities.invokeLater(this::loadCandles));
		}
		return cmbSymbols;
	}

	private void loadCandles() {
		String symbol = validateSymbol();
		if (symbol == null) {
			return;
		}

		try {
			Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
			Instant to = Instant.now().minus(30, ChronoUnit.DAYS).plus(2, ChronoUnit.DAYS);

			List<Candle> candles = Collections.list(candleRepository.iterate(symbol, from, to, true));
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

	public static void main(String... args) {
		DatabaseConfiguration databaseConfiguration = new SimulationConfiguration().database();
		CandleRepository candleRepository = new CandleRepository(databaseConfiguration);

		SymbolSelector symbolSelector = new SymbolSelector(candleRepository, new CandleHistory());
		WindowUtils.displayTestFrame(symbolSelector);
	}
}
