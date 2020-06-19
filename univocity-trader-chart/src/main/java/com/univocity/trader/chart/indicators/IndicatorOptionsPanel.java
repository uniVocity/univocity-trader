package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.gui.*;
import com.univocity.trader.indicators.base.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;

class IndicatorOptionsPanel extends JPanel {

	static final String PREVIEW_UPDATED = "PREVIEW_UPDATED";
	static final String INDICATOR_UPDATED = "INDICATOR_UPDATED";

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
		if (this.indicatorDefinition == indicatorDefinition) {
			return;
		}
		this.indicatorDefinition = indicatorDefinition;

		ContainerUtils.clearPanel(this);
		if (indicatorDefinition != null) {
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weighty = 0.0;
			c.insets = new Insets(5, 5, 5, 5);
			c.anchor = GridBagConstraints.WEST;
			c.gridy = 0;
			indicatorDefinition.parameters.forEach(this::addComponent);
		}

		if (visualIndicatorPreview != null) {
			updatePreview(null);
		}

		revalidate();
		repaint();
	}

	private void addComponent(Argument argument) {
		JLabel lbl = new JLabel(argument.name);
		JComponent input = argument.getComponent();

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


	void updatePreview(ActionEvent e) {
		if(indicatorDefinition != null) {
			VisualIndicator old = visualIndicatorPreview;
			visualIndicatorPreview = new VisualIndicator(interval, indicatorDefinition);
			firePropertyChange(PREVIEW_UPDATED, old, visualIndicatorPreview);
		}
	}
}
