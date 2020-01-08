package com.univocity.trader.chart.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.lang.reflect.*;

public class ContainerUtils {

	public static void setAllComponentsEnabled(boolean enabled, Container c) {
		for (Component component : c.getComponents()) {
			component.setEnabled(enabled);
			if (component instanceof Container) {
				setAllComponentsEnabled(enabled, (Container) component);
			}
		}
	}

	public static void setAllBordersEnabled(boolean enabled, Container c, Color borderColor) {
		setBorderEnabled(enabled, c, borderColor);
		for (Component component : c.getComponents()) {
			if (component instanceof Container) {
				setBorderEnabled(enabled, (Container) component, borderColor);
			} else {
				setBorderEnabled(enabled, component, borderColor);
			}
		}
	}

	public static void setBorderEnabled(boolean enabled, Component c, Color borderColor) {
		if (!enabled) {
			borderColor = Color.GRAY;
		}
		if (c instanceof JComponent) {
			Border border = ((JComponent) c).getBorder();
			if (border != null) {
				if (border instanceof TitledBorder) {
					TitledBorder titledBorder = ((TitledBorder) border);
					titledBorder.setTitleColor(borderColor);
					border = titledBorder.getBorder();
				}
				if (border instanceof LineBorder) {
					try {
						// gambi pra pegar a propriedade
						Field lineColor = LineBorder.class.getField("lineColor");
						lineColor.setAccessible(true);
						lineColor.set(border, borderColor);
					} catch (Exception ex) {
						// ingora e foda-se
					}
				}
				c.repaint();
			}
		}
	}
	
	public static JPanel createVerticalPanel(Component ... components){
		JPanel out = new JPanel(new GridBagLayout());
		addComponents(out, components);
		return out;
	}
	
	public static void clearPanel(JPanel panel){
		panel.removeAll();
		panel.setBorder(null);
		panel.revalidate();
		panel.repaint();		
	}
	
	public static void addComponents(JPanel panel, Component ... components){
		panel.removeAll();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.anchor = GridBagConstraints.NORTH;
		
		if(components.length >= 1){
			panel.add(components[0], c);
		}
		
		for(int i = 1; i < components.length -1; i++){
			c.gridy++;
			panel.add(components[i], c);
		}
		
		if(components.length > 1){
			c.gridy++;
			c.weighty = 1.0;		
			panel.add(components[components.length - 1], c);
		}
		
		panel.revalidate();
		panel.repaint();
	}
	
}
