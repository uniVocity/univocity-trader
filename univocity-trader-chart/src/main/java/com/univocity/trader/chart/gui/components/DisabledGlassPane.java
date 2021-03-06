package com.univocity.trader.chart.gui.components;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DisabledGlassPane extends JComponent implements KeyListener {

	private final static Color DEFAULT_BACKGROUND = new Color(128, 128, 128, 128);
	private final static Border MESSAGE_BORDER = new EmptyBorder(10, 10, 10, 10);

	private JLabel message = new JLabel();

	public DisabledGlassPane() {
		setOpaque(false);
		setBackground(DEFAULT_BACKGROUND);
		setLayout(new GridBagLayout());
		add(message, new GridBagConstraints());

		message.setOpaque(true);
		message.setBorder(MESSAGE_BORDER);

		addMouseListener(new MouseAdapter() {
		});
		addMouseMotionListener(new MouseMotionAdapter() {
		});

		addKeyListener(this);

		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
	}

        @Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);
	}

        @Override
	public void setBackground(Color background) {
		super.setBackground(background);

		Color messageBackground = new Color(background.getRed(), background.getGreen(), background.getBlue());
		message.setBackground(messageBackground);
	}


	public void keyPressed(KeyEvent e) {
		e.consume();
	}


	public void keyTyped(KeyEvent e) {
		e.consume();
	}

	public void keyReleased(KeyEvent e) {
		e.consume();
	}

	public void activate(){
		activate(null, true);
	}
	
	public void activate(boolean waitCursor){
		activate(null, false);
	}

	public void activate(String text) {
		activate(text, true);
	}
	
	public void activate(String text, boolean waitCursor) {
		if (text != null && text.length() > 0) {
			message.setVisible(true);
			message.setText(text);
		} else
			message.setVisible(false);

		setVisible(true);
		if(waitCursor){
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		requestFocusInWindow();
	}

	public void deactivate() {
		if(!isVisible()){
			return;
		}
		SwingUtilities.invokeLater(() -> {
			setCursor(null);
			setVisible(false);
		});
	}

	public static void main(String[] args) {
		final DisabledGlassPane glassPane = new DisabledGlassPane();
		glassPane.setBackground(new Color(125, 125, 125, 125));
		glassPane.setForeground(Color.BLUE);

		final JTextField textField = new JTextField();

		final JButton button = new JButton("Click Me");
		button.setMnemonic('c');
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				glassPane.activate("Please Wait...");

				Thread thread = new Thread(() -> {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
					glassPane.deactivate();
				});
				thread.start();
			}
		});

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setGlassPane(glassPane);
		frame.getContentPane().add(new JLabel("NORTH"), BorderLayout.NORTH);
		frame.getContentPane().add(button);
		frame.getContentPane().add(new JTextField(), BorderLayout.SOUTH);
		frame.getContentPane().add(textField, BorderLayout.SOUTH);
		frame.setSize(300, 300);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
