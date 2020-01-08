package com.univocity.trader.chart;

import com.univocity.trader.candles.*;

import javax.swing.*;
import java.awt.*;

public class ChartWindow extends JFrame {

	private LineChart chart;

	public ChartWindow() {
		this.setLayout(new BorderLayout());
		this.setTitle("No candle data loaded");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, 800, 600);
		this.setLocationRelativeTo(null);

		this.chart = new LineChart();
		this.add(chart, BorderLayout.CENTER);

		JButton addCandle = new JButton("Add candle");
		this.add(addCandle, BorderLayout.SOUTH);

		addCandles();

		addCandle.addActionListener(e -> {
			double v = (Math.random() * 5.0);
			int size = chart.tradeHistory.size();
			chart.tradeHistory.add(new Candle(size * 60000, (size + 1) * 60000, v + 1, v + 1.1, v + 0.9, v + 1.09, 1));
			chart.dataUpdated();

		});

	}

	private void addCandles() {
		for (int i = 0; i < 50; i++) {
			double v = (Math.random() * 5.0);
			chart.tradeHistory.add(new Candle(i * 60000, (i + 1) * 60000, v, v + 1.1, v + 0.9, v + 1.09, 1));
		}
		chart.dataUpdated();
	}

	public static void main(String... args) {
		new ChartWindow().setVisible(true);
	}
}
