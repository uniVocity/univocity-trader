package com.univocity.trader.chart;


import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.chart.gui.*;
import com.univocity.trader.chart.gui.components.*;
import com.univocity.trader.chart.gui.components.time.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

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
	private Aggregator aggregator;

	private CandleRepository candleRepository;
	private final CandleHistory candleHistory;

	private DisabledGlassPane glassPane;
	private DateEditPanel chartStart;
	private DateEditPanel chartEnd;
	private JButton btLoad;
	private ExchangeSelector exchangeSelector;
	private JButton btUpdate;
	private JToggleButton btLive;
	private List<Consumer<Candle>> liveFeedConsumers = new ArrayList<>();

	public SymbolSelector(CandleHistory candleHistory) {
		this.candleHistory = candleHistory;

		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		this.add(getExchangeSelector(), c);

		c.gridx = 1;
		this.add(getCmbSymbols(), c);

		c.gridx = 2;
		this.add(getTxtUnits(), c);

		c.gridx = 3;
		this.add(getCmbUnitType(), c);

		c.gridx = 4;
		this.add(getBtLoad(), c);

		c.gridx = 5;
		this.add(getBtUpdate(), c);

		c.gridx = 6;
		this.add(getBtLive(), c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		this.add(getChartStart(), c);

		c.gridx = 3;
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

	private JToggleButton getBtLive() {
		if (btLive == null) {
			btLive = new JToggleButton("Connect");
			btLive.setEnabled(false);
			btLive.addActionListener(l -> tradeLive());
		}
		return btLive;
	}

	private void tradeLive() {
		if (getBtLive().isSelected()) {
			getBtLive().setText("Disconnect");
			String symbol = getSymbol();
			if (symbol != null) {
				new Thread(() -> {
					startStream(getExchange(true), symbol);
				}).start();
			}
		} else {
			getExchange(true);
			getBtLive().setText("Connect");
		}
	}

	private Exchange getExchange(boolean disconnect) {
		Exchange exchange = getExchangeSelector().getSelectedExchange();
		if (disconnect) {
			try {
				exchange.closeLiveStream();
				fireLiveFeedConsumers(null);
			} catch (Exception e) {
				log.error("Error closing live stream from " + exchange.getName());
			}
		}
		return exchange;
	}

	private void fireLiveFeedConsumers(Candle tick) {
		for (Consumer<Candle> consumer : liveFeedConsumers) {
			consumer.accept(tick);
		}
	}

	private void loadRecentCandles(Exchange exchange, String symbol, boolean updateCandleHistory) {
		IncomingCandles recent = exchange.getLatestTicks(symbol, TimeInterval.MINUTE);
		for (Object tick : recent) {
			if (tick != null) {
				PreciseCandle latestCandle = exchange.generatePreciseCandle(tick);
				candleRepository.addToHistory(symbol, latestCandle, true);
				if (updateCandleHistory) {
					candleHistory.addOrUpdateSilently(new Candle(latestCandle));
				}
			}
		}
	}

	private void startStream(Exchange exchange, String symbol) {
		if (!(candleRepository instanceof DatabaseCandleRepository)) {
			WindowUtils.displayWarning(this, "Can't open live stream for symbol " + symbol + " in " + exchange.getName() + ". No database active to process data.");
			return;
		}
		backfill(exchange, false);

		SwingUtilities.invokeLater(() -> getGlassPane().activate("Starting live data stream for " + getSymbol()));
		new Thread(() -> startStream((DatabaseCandleRepository) candleRepository, exchange, symbol)).start();

	}

	private void startStream(DatabaseCandleRepository repository, Exchange exchange, String symbol) {
		loadRecentCandles(exchange, symbol, false);
		fillAvailableDates();
		loadCandles();

		exchange.openLiveStream(symbol.toLowerCase(), TimeInterval.MINUTE, new TickConsumer() {

			boolean initializing = true;

			@Override
			public void tickReceived(String symbol, Object rawTick) {
				PreciseCandle tickInStream = exchange.generatePreciseCandle(rawTick);
				Candle tick = new Candle(tickInStream);
				updateChartEnd(tick);

				if (initializing) {
					synchronized (this) {
						if (initializing) {
							try {
								loadRecentCandles(exchange, symbol, true);
								Candle last = candleHistory.getLast();
								initializing = tick.openTime - last.openTime > TimeInterval.MINUTE.ms;
							} finally {
								if (!initializing) {
									getGlassPane().deactivate();
								}
							}
						}
					}
				}

				repository.addToHistory(symbol.toUpperCase(), tickInStream, false);


				fireLiveFeedConsumers(tick);

				Aggregator aggregator = getAggregator(false);

				aggregator.aggregate(tick);
				if ((tick = aggregator.getFull()) != null) {
					candleHistory.addOrUpdate(tick);
				}

				//tick.close;
			}

			@Override
			public void streamError(Throwable cause) {
				log.error("Error receiving live data from exchange", cause);
				getGlassPane().deactivate();
			}

			@Override
			public void streamClosed() {
				log.info("Closing live data stream");
				getGlassPane().deactivate();
			}
		});
	}

	private ExchangeSelector getExchangeSelector() {
		if (exchangeSelector == null) {
			this.exchangeSelector = new ExchangeSelector();
			this.exchangeSelector.addExchangeListener(this::exchangeSelected);
		}
		return exchangeSelector;
	}

	private void exchangeSelected(LiveTrader<?, ?, ?> liveTrader) {
		getCmbSymbols().setEnabled(false);
		getBtLive().setEnabled(false);

		String currentSymbol = getSymbol();

		this.candleRepository = liveTrader.candleRepository();
		getCmbSymbolsModel().removeAllElements();

		Set<String> available = candleRepository.getKnownSymbols();
		getCmbSymbolsModel().addAll(available);

		if (currentSymbol != null && available.contains(currentSymbol)) {
			System.out.println(currentSymbol);
			getCmbSymbolsModel().setSelectedItem(currentSymbol);
		}

		getCmbSymbols().setEnabled(true);
	}

	private JButton getBtUpdate() {
		if (btUpdate == null) {
			btUpdate = new JButton("Update");
			btUpdate.setEnabled(false);
			btUpdate.addActionListener((e) -> SwingUtilities.invokeLater(this::executeBackfill));
		}
		return btUpdate;
	}

	private void executeLoadCandles() {
		Thread thread = new Thread(() -> {
			try {
				getGlassPane().activate("Loading " + getSymbol() + " candles...");
				loadCandles();
			} finally {
				glassPane.deactivate();
			}
		});
		thread.start();
	}

	private void executeBackfill() {
		if (!candleRepository.isWritingSupported()) {
			log.warn("Can't backfill data. Writing not supported");
			return;
		}

		Exchange<?, ?> exchange = getExchangeSelector().getSelectedExchange();
		if (exchange == null) {
			getBtUpdate().setEnabled(false);
			return;
		}
		new Thread(() -> backfill(exchange, true)).start();
	}

	private void backfill(Exchange<?, ?> exchange, boolean loadCandles) {
		try {
			glassPane.activate("Updating history of " + getSymbol());
			CandleHistoryBackfill backfill = new CandleHistoryBackfill((DatabaseCandleRepository) candleRepository);
			backfill.fillHistoryGaps(exchange, getSymbol(), getChartStart().getCommittedValue(), Instant.now(), TimeInterval.minutes(1));
			fillAvailableDates();
			if (loadCandles) {
				loadCandles();
			}
		} finally {
			glassPane.deactivate();
		}
	}

	public String getSymbol() {
		String symbol = (String) getCmbSymbols().getSelectedItem();
		if (getCmbSymbols().getSelectedIndex() == -1) {
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
			cmbSymbols = new JComboBox<>(getCmbSymbolsModel());
			cmbSymbols.setEnabled(false);
			cmbSymbols.setEditable(true);
			cmbSymbols.setSelectedIndex(-1);
			cmbSymbols.addActionListener((e) -> getBtLoad().setEnabled(true));
			cmbSymbols.addActionListener((e) -> getBtLive().setEnabled(true));
			cmbSymbols.addActionListener((e) -> fillAvailableDates());
		}
		return cmbSymbols;
	}

	private DefaultComboBoxModel<String> getCmbSymbolsModel() {
		if (cmbSymbolsModel == null) {
			cmbSymbolsModel = new DefaultComboBoxModel<>();
		}
		return cmbSymbolsModel;
	}

	private Aggregator getAggregator(boolean reset) {
		if (aggregator == null || reset) {
			TimeInterval interval = getInterval();
			aggregator = new Aggregator("").getInstance(interval);
		}
		return aggregator;
	}

	private void loadCandles() {
		btUpdate.setEnabled(false);
		btLoad.setEnabled(false);
		String symbol = validateSymbol();
		if (symbol == null) {
			return;
		}

		try {
			Instant from = getChartStart().getCommittedValue();
			Instant to = getChartEnd().getCommittedValue();

			long count = candleRepository.countCandles(symbol, from, to);
			List<Candle> candles = new ArrayList<>((int) count);

			Aggregator aggregator = getAggregator(true);

			Enumeration<Candle> data = candleRepository.iterate(symbol, from, to, false);
			while (data.hasMoreElements()) {
				Candle candle = data.nextElement();
				if (candle != null) {
					aggregator.aggregate(candle);
					if ((candle = aggregator.getFull()) != null) {
						candles.add(candle);
					}
				}
			}
			if (aggregator.getPartial() != null) {
				candles.add(aggregator.getPartial());
			}

			if (candles.size() == 0) {
				WindowUtils.displayWarning(this, "No history data available for symbol " + symbol);
			}
			candleHistory.setCandles(candles);
			btUpdate.setEnabled(true);
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
			chartStart.addDateEditPanelListener(e -> enableBtLoad());
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
			chartEnd.addDateEditPanelListener(e -> enableBtLoad());
			chartEnd.setEnabled(false);
			chartEnd.setInferLeastPossibleValue(false);
			getChartStart().addDateEditPanelListener(e -> chartEnd.setMinimumValue(e.getNewDate()));
		}
		return chartEnd;
	}

	private void fillAvailableDates() {
		String symbol = getSymbol();
		setDateSelectionEnabled(symbol != null);
		if (symbol != null) {
			Candle first = candleRepository.firstCandle(symbol);
			if (first != null) {
				getChartStart().setMinimumValue(first.openTime);
				getChartStart().setValue(first.openTime);

				Candle last = candleRepository.lastCandle(symbol);
				updateChartEnd(last);

				updateDateFields();
			}
		}
	}

	private void updateChartEnd(Candle tick) {
		getChartEnd().setMaximumValue(tick.closeTime);
		getChartEnd().setValue(tick.closeTime);
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

	private void updateDateFields() {
		if (getChartStart().isEnabled() && getChartEnd().isEnabled()) {
			TimeIntervalType timeInterval = (TimeIntervalType) this.cmbUnitType.getSelectedItem();
			getChartStart().setEnabled(false, DateEditPanel.ALL_FIELDS);
			getChartEnd().setEnabled(false, DateEditPanel.ALL_FIELDS);
			getChartStart().setEnabled(true, getEnabledFields(timeInterval));
			getChartEnd().setEnabled(true, getEnabledFields(timeInterval));
		}
	}

	private JComboBox<TimeIntervalType> getCmbUnitType() {
		if (cmbUnitType == null) {
			cmbUnitType = new JComboBox<>();
			cmbUnitType.setModel(new DefaultComboBoxModel<>(TimeIntervalType.values()));
			cmbUnitType.setSelectedItem(TimeIntervalType.DAY);
			cmbUnitType.addActionListener((e) -> btLoad.setEnabled(true));
			cmbUnitType.addItemListener(evt -> updateDateFields());
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
			SpinnerNumberWrapModel model = new SpinnerNumberWrapModel(txtUnits, 1, 1, 999999, 1);
			txtUnits.setModel(model);
			txtUnits.addChangeListener(e -> enableBtLoad());
		}
		return txtUnits;
	}

	private void enableBtLoad() {
		getBtLoad().setEnabled(getSymbol() != null && getInterval() != null);
	}

	public TimeInterval getInterval() {
		Integer units = (Integer) getTxtUnits().getValue();
		TimeIntervalType type = (TimeIntervalType) getCmbUnitType().getSelectedItem();
		return type.toTimeInterval(units);
	}

	public void addLiveFeedConsumer(Consumer<Candle> feedConsumer) {
		liveFeedConsumers.add(feedConsumer);
	}

	public static void main(String... args) {
		DatabaseConfiguration databaseConfiguration = new SimulationConfiguration().database();
		DatabaseCandleRepository candleRepository = new DatabaseCandleRepository(databaseConfiguration);

//		CandleRepository candleRepository = new CandleRepository(databaseConfiguration);

		SymbolSelector symbolSelector = new SymbolSelector(new CandleHistory());
		WindowUtils.displayTestFrame(symbolSelector);
	}


}
