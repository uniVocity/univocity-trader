package com.univocity.trader.chart;

import com.univocity.trader.*;
import com.univocity.trader.chart.gui.*;
import com.univocity.trader.config.*;
import nonapi.io.github.classgraph.utils.*;
import org.slf4j.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class ExchangeSelector extends JComboBox<String> {

	private static final Logger log = LoggerFactory.getLogger(ExchangeSelector.class);

	private final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
	private final Map<String, Class<? extends EntryPoint>> entryPoints = new TreeMap<>();
	private final Set<String> disabledEntries = ConcurrentHashMap.newKeySet();

	private String currentSelection;
	private LiveTrader<?, ?, ?> selectedLiveTrader = null;

	private List<Consumer<LiveTrader<?, ?, ?>>> exchangeSelectionListeners = new ArrayList<>();

	public ExchangeSelector() {
		loadAvailableExchanges();
		setModel(model);

		this.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				updateSelectedExchange();
			}
		});

		updateSelectedExchange();
	}

	public void addExchangeListener(Consumer<LiveTrader<?, ?, ?>> listener) {
		exchangeSelectionListeners.add(listener);
	}

	private void loadAvailableExchanges() {
		for (Class<? extends EntryPoint> c : Utils.findClassesImplementing(EntryPoint.class)) {
			entryPoints.put(c.getSimpleName(), c);
			model.addElement(c.getSimpleName());
		}
	}

	public LiveTrader<?, ?, ?> getSelectedLiveTrader() {
		return selectedLiveTrader;
	}

	public Exchange<?, ?> getSelectedExchange() {
		if(selectedLiveTrader == null){
			return null;
		}
		return selectedLiveTrader.exchange();
	}

	void updateSelectedExchange() {
		String selection = (String) model.getSelectedItem();
		if (selection != null && Objects.equals(selection, currentSelection)) {
			return;
		}

		if (disabledEntries.contains(selection)) {
			model.setSelectedItem(currentSelection);
			return;
		}

		setEnabled(false);
		new Thread(() -> {
			if (selection != null) {
				Class<? extends EntryPoint> e = entryPoints.get(selection);
				if (e != null) {
					LiveTrader<?, ?, ?> trader = null;
					try {
						EntryPoint entryPoint = Utils.instantiate(e);
						trader = (LiveTrader<?, ?, ?>) ReflectionUtils.invokeMethod(entryPoint, "trader", true);
						Exchange<?, ?> selected = trader.exchange();
						if (selected != null) {
							selectedLiveTrader = trader;
							currentSelection = selection;
							exchangeSelected();
						}
					} catch (Throwable ex) {
						disabledEntries.add(selection);
						log.warn("Error loading exchange " + selection + ". Entry disabled.", ex);
					} finally {
						SwingUtilities.invokeLater(() -> setEnabled(true));
						if (trader != null) {
							trader.close();
						}
					}
				}
			}
		}).start();
	}

	private void exchangeSelected() {
		SwingUtilities.invokeLater(() -> {
			for (Consumer<LiveTrader<?, ?, ?>> listener : exchangeSelectionListeners) {
				try {
					listener.accept(selectedLiveTrader);
				} catch (Exception e) {
					log.error("Error notifying listener " + listener + " of exchange update", e);
				}
			}
		});

	}

	public static void main(String... args) {
		SwingUtilities.invokeLater(() -> WindowUtils.displayTestFrame(new ExchangeSelector()));
	}
}
