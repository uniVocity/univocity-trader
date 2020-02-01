package com.univocity.trader.chart.dynamic;

import com.univocity.trader.chart.gui.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ColorSelector extends JLabel {

	public static final String SELECTED_COLOR_CHANGED_PROPERTY = "SELECTED_COLOR_CHANGED_PROPERTY";

	public ColorSelector(final Color initialColor, final String colorChooserTitle) {
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setBackground(initialColor);
		setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		setOpaque(true);

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				// Color selectedColor = JColorChooser.showDialog(parent == null ?
				// ColorSelector.this : parent, colorChooserTitle, getSelectedColor());
				Color selectedColor = WindowUtils.displayJColorChooser(ColorSelector.this, colorChooserTitle,
						getSelectedColor());
				Color oldValue = getSelectedColor();
				if (selectedColor != null && selectedColor != oldValue) {
					setBackground(selectedColor);
					repaint();
					firePropertyChange(SELECTED_COLOR_CHANGED_PROPERTY, oldValue, selectedColor);
				}
			}
		});
	}

	public Color getSelectedColor() {
		return getBackground();
	}

	public void setSelectedColor(Color color) {
		this.setBackground(color);
	}
}
