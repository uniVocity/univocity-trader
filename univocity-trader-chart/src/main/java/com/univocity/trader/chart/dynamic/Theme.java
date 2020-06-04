package com.univocity.trader.chart.dynamic;

import com.univocity.trader.chart.charts.painter.*;

import javax.swing.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface Theme extends Repaintable {
	JPanel getThemeSettingsPanel();
}
