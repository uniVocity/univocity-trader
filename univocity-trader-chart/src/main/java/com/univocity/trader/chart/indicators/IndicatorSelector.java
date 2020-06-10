package com.univocity.trader.chart.indicators;

import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import javax.swing.*;
import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.function.*;

import static com.univocity.trader.chart.indicators.IndicatorOptionsPanel.*;

public class IndicatorSelector extends JPanel {

	private JComboBox<IndicatorDefinition> cmbIndicators;
	private DefaultComboBoxModel<IndicatorDefinition> indicators;
	private IndicatorOptionsPanel indicatorOptions;
	private final Set<IndicatorDefinition> availableIndicators = new TreeSet<>();
	private final Supplier<TimeInterval> timeInterval;

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
		this.timeInterval = timeInterval;
	}

	private IndicatorOptionsPanel getIndicatorOptionsPanel() {
		if (indicatorOptions == null) {
			indicatorOptions = new IndicatorOptionsPanel();
		}
		return indicatorOptions;
	}

	public void addIndicatorListener(IndicatorListener listener) {
		getIndicatorOptionsPanel().addPropertyChangeListener(PREVIEW_UPDATED, e -> invokeListener(listener, e, true));
		getIndicatorOptionsPanel().addPropertyChangeListener(INDICATOR_UPDATED, e -> invokeListener(listener, e, false));
	}

	private void invokeListener(IndicatorListener listener, PropertyChangeEvent e, boolean preview) {
		listener.indicatorUpdated(preview, (VisualIndicator) e.getOldValue(), (VisualIndicator) e.getNewValue());
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
			cmbIndicators.addItemListener(e -> SwingUtilities.invokeLater(() -> getIndicatorOptionsPanel().updateIndicator((IndicatorDefinition) cmbIndicators.getSelectedItem(), timeInterval)));
		}
		return cmbIndicators;
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
