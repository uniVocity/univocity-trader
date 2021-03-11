package com.univocity.trader.chart.gui.components;

import javax.swing.text.*;

public class UppercaseDocumentFilter extends DocumentFilter {
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
		fb.insertString(offset, text.toUpperCase(), attr);
	}

	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
		fb.replace(offset, length, text.toUpperCase(), attrs);
	}
}