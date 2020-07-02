package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.charts.painter.Painter;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
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

	private VisualIndicator editing;
	private VisualIndicator preview;

	private class Updater implements ChangeListener, ItemListener, ActionListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			indicatorChanged();
			Object source = e.getSource();
			if (source instanceof JSpinner) {
				((JSpinner) source).requestFocus();
			}
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			indicatorChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			indicatorChanged();
		}
	}

	final Updater previewUpdater = new Updater();

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
			btAdd = new JButton("New indicator");
			btAdd.addActionListener(e -> addIndicator());
		}
		return btAdd;
	}

	private JButton getBtRemove() {
		if (btRemove == null) {
			btRemove = new JButton("Remove");
			btRemove.addActionListener(e -> removeIndicator());
			btRemove.setEnabled(false);
		}
		return btRemove;
	}


	private IndicatorOptionsPanel getIndicatorOptionsPanel() {
		if (indicatorOptions == null) {
			indicatorOptions = new IndicatorOptionsPanel(this);
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
			cmbIndicators.addItemListener(previewUpdater);
			cmbIndicators.setEnabled(false);
		}
		return cmbIndicators;
	}

	public void recalculateIndicators() {
		updatePreview();
	}

	public void displayOptionsFor(Painter<?> painter) {
		if (painter instanceof VisualIndicator) {
			displayOptionsFor((VisualIndicator) painter);
		}
	}

	public void displayOptionsFor(VisualIndicator i) {
		editing = preview = i;
		getCmbIndicators().getModel().setSelectedItem(i.config);
		getCmbIndicators().setEnabled(false);

		addingIndicator();
		getBtAdd().setText("Close");
		getBtAdd().setEnabled(true);
		getBtRemove().setEnabled(true);
		getBtRemove().setText("Remove");

		i.updateEditorValues();
	}

	void updatePreview() {
		SwingUtilities.invokeLater(this::indicatorChanged);
	}

	private void fireIndicatorUpdated(boolean preview, VisualIndicator old, VisualIndicator newIndicator) {
		indicatorListenerList.forEach(l -> l.indicatorUpdated(preview, old, newIndicator));
	}

	private void indicatorChanged() {
		IndicatorDefinition indicatorDefinition = (IndicatorDefinition) cmbIndicators.getSelectedItem();
		if (getIndicatorOptionsPanel().updateIndicator(indicatorDefinition)) {
			updatePreview(indicatorDefinition);
			getBtAdd().setEnabled(true);
		}
	}

	void updatePreview(IndicatorDefinition indicatorDefinition) {
		if (indicatorDefinition != null) {
			VisualIndicator old = preview;
			preview = new VisualIndicator(timeInterval, indicatorDefinition);
			if (editing != null) {
				preview.position(editing.position());
			}
			fireIndicatorUpdated(editing == null, old, preview);
		}
	}

	void addIndicator() {
		if (preview != null) {
			fireIndicatorUpdated(true, preview, null); //remove preview
			fireIndicatorUpdated(false, null, new VisualIndicator(timeInterval, preview.config)); //add "persistent" indicator
			editingStopped();
		} else {
			addingIndicator();
		}

		editing = null;
		preview = null;
	}

	private void addingIndicator() {
		getBtAdd().setText("Add");
		getBtAdd().setEnabled(false); //enable after selecting indicator

		getBtRemove().setText("Cancel");
		getBtRemove().setEnabled(true);

		getCmbIndicators().setEnabled(true);
	}

	private void editingStopped() {
		getBtAdd().setText("New Indicator");
		getBtAdd().setEnabled(true);

		getBtRemove().setText("Remove");
		getBtRemove().setEnabled(false);

		getCmbIndicators().setSelectedItem(null);
		getCmbIndicators().setEnabled(false);
	}

	void removeIndicator() {
		if (preview != null) {
			fireIndicatorUpdated(false, preview, null);
		}

		editingStopped();

		preview = null;
		editing = null;
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
