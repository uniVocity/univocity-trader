package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.gui.*;
import com.univocity.trader.indicators.base.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.*;

class IndicatorOptionsPanel extends JPanel {

	private IndicatorDefinition indicatorDefinition;
	private GridBagConstraints c;

	public IndicatorOptionsPanel() {
		super(new GridBagLayout());
	}

	public boolean updateIndicator(IndicatorDefinition indicatorDefinition) {
		if(indicatorDefinition == null && this.indicatorDefinition != null){
			this.indicatorDefinition = null;
			ContainerUtils.clearPanel(this);
			return false;
		}
		this.indicatorDefinition = indicatorDefinition;

		ContainerUtils.clearPanel(this);
		if (indicatorDefinition != null) {
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weighty = 0.0;
			c.insets = new Insets(2, 2, 2, 2);
			c.anchor = GridBagConstraints.WEST;
			c.gridy = 0;
			indicatorDefinition.parameters.forEach(this::addComponent);
		}

		revalidate();
		repaint();
		return true;
	}

	private void addComponent(Argument argument) {
		JLabel lbl = new JLabel(argument.name);
		JComponent input = argument.getComponent();

		if (input != null) {
			c.gridx = 0;
			c.weightx = 1.0;
			add(lbl, c);

			c.gridx = 1;
			c.weightx = 1.0;
			add(input, c);

			c.gridy++;
		}
		c.gridx = 0;
		c.gridwidth = 2;
	}
}
