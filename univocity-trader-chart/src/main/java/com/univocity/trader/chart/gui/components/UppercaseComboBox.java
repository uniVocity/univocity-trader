package com.univocity.trader.chart.gui.components;

import org.apache.commons.lang3.*;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;

public class UppercaseComboBox extends JComboBox<String> {

	private DefaultComboBoxModel<String> model;

	{
		setEditable(true);
		setSelectedIndex(-1);
		JTextField t = (JTextField) getEditor().getEditorComponent();
		((AbstractDocument) t.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
	}

	public UppercaseComboBox(ComboBoxModel<String> aModel) {
		super(aModel);
	}

	public UppercaseComboBox(String[] items) {
		super(items);
	}

	public UppercaseComboBox(Vector<String> items) {
		super(items);
	}

	public UppercaseComboBox() {
	}

	@Override
	public String getSelectedItem() {
		String item = (String) super.getSelectedItem();
		if (getSelectedIndex() == -1) {
			item = (String) getEditor().getItem();
			if (StringUtils.isNotBlank(item)){
				item = item.trim();
				addItem(item);
			}
		}

		if (StringUtils.isNotBlank(item)) {
			return item.toUpperCase();
		}
		return null;
	}

	public DefaultComboBoxModel<String> getModel() {
		if (model == null) {
			model = new DefaultComboBoxModel<>();
		}
		return model;
	}

	private void updateSymbols() {
		String item = getSelectedItem();
		if (item != null) {
			int symbolCount = model.getSize();
			for (int i = 0; i < symbolCount; i++) {
				if (model.getElementAt(i).equals(item)) {
					return;
				}
			}
			model.addElement(item);
		}
	}

	public void setSelectedItem(String symbol) {
		String current = getSelectedItem();
		if (current == null || !current.equals(symbol)) {
			super.setSelectedItem(symbol);
		}
	}

	public void setItems(Collection<String> newElements) {
		String currentSymbol = getSelectedItem();

		model.removeAllElements();
		model.addAll(newElements);

		if (currentSymbol != null && newElements.contains(currentSymbol)) {
			model.setSelectedItem(currentSymbol);
		}
	}
}
