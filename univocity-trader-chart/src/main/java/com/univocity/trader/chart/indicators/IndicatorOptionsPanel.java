package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.gui.*;
import com.univocity.trader.indicators.base.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.*;
import java.util.*;
import java.util.function.*;

class IndicatorOptionsPanel extends JPanel {

	static final String PREVIEW_UPDATED = "PREVIEW_UPDATED";
	static final String INDICATOR_UPDATED = "INDICATOR_UPDATED";

	private Map<String, Supplier<Object>> values = new HashMap<>();
	private IndicatorDefinition indicatorDefinition;
	private GridBagConstraints c;

	private VisualIndicator visualIndicatorPreview;

	private JButton btUpdate;
	private Supplier<TimeInterval> interval;


	public IndicatorOptionsPanel() {
		super(new GridBagLayout());

		btUpdate = new JButton("Update");
		btUpdate.addActionListener(this::updatePreview);
	}

	public void updateIndicator(IndicatorDefinition indicatorDefinition, Supplier<TimeInterval> interval) {
		this.interval = interval;
		values.clear();
		this.indicatorDefinition = indicatorDefinition;

		ContainerUtils.clearPanel(this);
		if (indicatorDefinition != null) {
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weighty = 0.0;
			c.insets = new Insets(5, 5, 5, 5);
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridy = 0;
			indicatorDefinition.argumentTypes.forEach(this::addComponent);
		}

		if (visualIndicatorPreview != null) {
			updatePreview(null);
		}

		revalidate();
		repaint();
	}

	private void addComponent(String label, Class<?> inputType) {
		JLabel lbl = new JLabel(label);
		JComponent input = null;
		if (inputType == double.class || inputType == Double.class || inputType == float.class || inputType == Float.class || inputType == BigDecimal.class) {
			input = getFloatingPointInput(label, inputType);
		} else if (inputType == long.class || inputType == Long.class || inputType == int.class || inputType == Integer.class || inputType == short.class || inputType == Short.class || inputType == byte.class || inputType == Byte.class) {
			input = getNumericInput(label, inputType);
		} else if (inputType == boolean.class || inputType == Boolean.class) {
			input = getBooleanInput(label);
		}

		if (input != null) {
			c.gridx = 0;
			c.weightx = 0.0;
			add(lbl, c);

			c.gridx = 1;
			c.weightx = 1.0;
			add(input, c);

			c.gridy++;
		}
		c.gridx = 0;
		c.gridwidth = 2;
		add(btUpdate, c);
	}

	private JSpinner getFloatingPointInput(String label, Class<?> type) {
		JSpinner out = new JSpinner(new SpinnerNumberModel());
		values.put(label, out::getValue);
		return out;
	}

	private JSpinner getNumericInput(String label, Class<?> type) {
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setValue(1);
		JSpinner out = new JSpinner(model);

		values.put(label, out::getValue);
		return out;
	}

	private JCheckBox getBooleanInput(String label) {
		JCheckBox out = new JCheckBox();
		values.put(label, out::isSelected);
		return out;
	}

	private void updatePreview(ActionEvent e) {
		Map<String, Object> params = new HashMap<>();
		values.forEach((k, v) -> params.put(k, v.get()));
		VisualIndicator old = visualIndicatorPreview;

		visualIndicatorPreview = new VisualIndicator(interval, () -> indicatorDefinition.create(params, interval.get()));

		firePropertyChange(PREVIEW_UPDATED, old, visualIndicatorPreview);
	}
}
