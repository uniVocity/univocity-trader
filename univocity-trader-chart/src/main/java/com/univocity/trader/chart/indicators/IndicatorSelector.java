package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.charts.painter.Painter;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

public class IndicatorSelector extends JPanel {

	private JComboBox<IndicatorDefinition> cmbIndicators;
	private DefaultComboBoxModel<IndicatorDefinition> indicators;
	private IndicatorOptionsPanel indicatorOptions;
	private final Set<IndicatorDefinition> availableIndicators = new TreeSet<>();
	private final Supplier<TimeInterval> timeInterval;

	private JPanel controlPanel;
	private JButton btAdd;
	private JButton btRemove;

	private VisualIndicator visualIndicatorPreview;

	private final List<IndicatorListener> indicatorListenerList = new ArrayList<>();

	public IndicatorSelector(Supplier<TimeInterval> timeInterval) {
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 5, 5, 5);
		this.add(getCmbIndicators(), c);

		c.gridy = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		this.add(getIndicatorOptionsPanel(), c);


		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;

		this.add(getControlPanel(), c);

		this.timeInterval = timeInterval;
	}

	private JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new JPanel();
			controlPanel.add(getBtAdd());
			controlPanel.add(getBtRemove());
		}
		return controlPanel;
	}

	private JButton getBtAdd() {
		if (btAdd == null) {
			btAdd = new JButton("Add");
			btAdd.addActionListener(e -> addIndicator());
		}
		return btAdd;
	}

	private JButton getBtRemove() {
		if (btRemove == null) {
			btRemove = new JButton("Remove");
			btRemove.addActionListener(this::updatePreview);
		}
		return btRemove;
	}


	private IndicatorOptionsPanel getIndicatorOptionsPanel() {
		if (indicatorOptions == null) {
			indicatorOptions = new IndicatorOptionsPanel();
		}
		return indicatorOptions;
	}

	public void addIndicatorListener(IndicatorListener listener) {
		this.indicatorListenerList.add(listener);
	}

	public IndicatorSelector loadIndicator(Class<? extends Indicator> indicator) {
		availableIndicators.addAll(IndicatorDefinition.loadConstructors(indicator));
		updateModel();
		return this;
	}

	public IndicatorSelector loadIndicatorsFrom(Class<?> indicatorFactory) {
		availableIndicators.addAll(IndicatorDefinition.loadIndicators(indicatorFactory));
		updateModel();
		return this;
	}

	private void updateModel() {
		Object selected = indicators.getSelectedItem();
		indicators.removeAllElements();
		indicators.addAll(availableIndicators);
		indicators.setSelectedItem(selected);
	}

	private JComboBox<IndicatorDefinition> getCmbIndicators() {
		if (cmbIndicators == null) {
			indicators = new DefaultComboBoxModel<>();
			cmbIndicators = new JComboBox<>(indicators);
			cmbIndicators.addItemListener(this::indicatorChanged);
		}
		return cmbIndicators;
	}

	public void recalculateIndicators() {
		updatePreview((ActionEvent) null);
	}


	public void displayOptionsFor(Painter<?> painter) {
		if (painter instanceof VisualIndicator) {
			displayOptionsFor((VisualIndicator) painter);
		}
	}

	public void displayOptionsFor(VisualIndicator i) {
		visualIndicatorPreview = i;
		getCmbIndicators().getModel().setSelectedItem(i.config);
	}

	void updatePreview(ActionEvent e) {
		indicatorChanged(null);
	}

	private void fireIndicatorUpdated(boolean preview, VisualIndicator old, VisualIndicator newIndicator) {
		indicatorListenerList.forEach(l -> l.indicatorUpdated(preview, old, newIndicator));
	}

	private void indicatorChanged(ItemEvent e) {
		IndicatorDefinition indicatorDefinition = (IndicatorDefinition) cmbIndicators.getSelectedItem();
		if (getIndicatorOptionsPanel().updateIndicator(indicatorDefinition)) {
			updatePreview(indicatorDefinition);
		}
	}

	void updatePreview(IndicatorDefinition indicatorDefinition) {
		if (indicatorDefinition != null) {
			VisualIndicator old = visualIndicatorPreview;
			visualIndicatorPreview = new VisualIndicator(timeInterval, indicatorDefinition);
			fireIndicatorUpdated(true, old, visualIndicatorPreview);
		}
	}

	void addIndicator() {
		if (visualIndicatorPreview != null) {
			fireIndicatorUpdated(true, visualIndicatorPreview, null); //remove preview
			fireIndicatorUpdated(false, null, new VisualIndicator(timeInterval, visualIndicatorPreview.config)); //add "persistent" indicator
			getCmbIndicators().setSelectedItem(null);
		}
	}

	public static void main(String... args) {
		JFrame f = new JFrame();
		f.setLayout(new BorderLayout());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 800, 220);
		f.add(new IndicatorSelector(() -> null).loadIndicatorsFrom(DefaultIndicators.class), BorderLayout.CENTER);
		f.setVisible(true);
	}
}
