package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.charts.painter.Painter;
import com.univocity.trader.chart.dynamic.code.*;
import com.univocity.trader.chart.gui.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import javax.swing.*;
import javax.swing.border.*;
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

	private JDialog dialog;

	private class Updater implements ChangeListener, ItemListener, ActionListener, UserCode.CodeUpdateListener {
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
		setBorder(new TitledBorder("Indicators"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		this.add(getCmbIndicators(), c);
		c.gridx = 1;
		this.add(getControlPanel(), c);

		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		this.add(getIndicatorOptionsPanel(), c);

		this.timeInterval = timeInterval;
	}

	private JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new JPanel(new GridLayout(1, 2, 5, 5));
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
			getDialog().setVisible(true);
		}
	}

	public void displayOptionsFor(VisualIndicator i) {
		editing = preview = i;
		getCmbIndicators().getModel().setSelectedItem(i.config);
		getCmbIndicators().setEnabled(false);

		addingIndicator();
		getBtAdd().setText("Done");
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
		SwingUtilities.invokeLater(() -> {
			IndicatorDefinition indicatorDefinition = (IndicatorDefinition) cmbIndicators.getSelectedItem();
			if (getIndicatorOptionsPanel().updateIndicator(indicatorDefinition)) {
				updatePreview(indicatorDefinition);
				getBtAdd().setEnabled(true);
			}
		});
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

	public JDialog getDialog() {
		if (dialog == null) {
			dialog = WindowUtils.createDialog("Indicators", this);
		}
		return dialog;
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
