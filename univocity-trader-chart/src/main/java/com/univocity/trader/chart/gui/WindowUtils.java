package com.univocity.trader.chart.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

public class WindowUtils {

	private static Rectangle getDisplayBounds(int windowCount, int rows, int cols) {
		windowCount--;
		Rectangle[] screens = DisplayObserver.getInstance().getScreenSizes();
		int displayIndex = windowCount / (rows * cols);
		if (displayIndex >= screens.length) {
			displayIndex = screens.length - 1;
		}
		return screens[displayIndex];
	}

	public static boolean isOnSameScreen(Component c1, Component c2) {
		Point p1 = c1.getLocationOnScreen();
		p1.x += c1.getWidth() / 2;
		p1.y += c1.getHeight() / 2;

		Point p2 = c2.getLocationOnScreen();
		p2.x += c2.getWidth() / 2;
		p2.y += c2.getHeight() / 2;

		Rectangle[] screens = DisplayObserver.getInstance().getScreenSizes();
		for (Rectangle screen : screens) {
			if (screen.contains(p1) && screen.contains(p2)) {
				return true;
			}
		}

		return false;
	}

	public static Rectangle getWindowBounds(int windowCount, int rows, int cols) {
		Rectangle display = getDisplayBounds(windowCount, rows, cols);

		windowCount--;
		int col = windowCount % cols;
		int row = (windowCount / cols) % rows;

		int height = display.height / rows;
		int width = display.width / cols;
		int x = col * width + display.x;
		int y = row * height + display.y;

		return new Rectangle(x, y, width, height);
	}

	public static Rectangle getWindowBoundsOnFirstScreen(int windowCount, int rows, int cols, Insets insets) {
		if ((windowCount - 1) / (rows * cols) <= 0) {
			return getWindowBounds(windowCount, rows, cols, insets);
		} else {
			return getWindowBounds(windowCount, rows, cols);
		}
	}

	public static Rectangle getWindowBounds(int windowCount, int rows, int cols, Insets insets) {
		Rectangle display = getDisplayBounds(windowCount, rows, cols);
		Rectangle bounds = getWindowBounds(windowCount, rows, cols);

		double horizontalProportion = 1.0 - ((insets.left + insets.right) / (double) display.getWidth());
		double verticalProportion = 1.0 - ((insets.top + insets.bottom) / (double) display.getHeight());

		bounds.x = (int) (((double) bounds.x * horizontalProportion) + insets.left);
		bounds.y = (int) (((double) bounds.y * verticalProportion) + insets.top);

		bounds.width *= horizontalProportion;
		bounds.height *= verticalProportion;

		return bounds;
	}

	public static int displayJOptionPane(Component parent, Object message, String title, int messageType, int optionType) {
		return displayJOptionPane(parent, message, title, messageType, optionType, null);
	}

	public static boolean displayConfirmation(Component parent, Object message, String title) {
		return displayJOptionPane(parent, message, title, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	public static boolean displayConfirmation(Component parent, Object message) {
		return displayConfirmation(parent, message, "Confirmation");
	}

	public static void displayWarning(Component parent, Object message) {
		displayWarning(parent, message, "Warning");
	}

	public static void displayWarning(Component parent, Object message, String title) {
		displayJOptionPane(parent, message, title, JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION);
	}

	public static void displayError(Component parent, Object message) {
		displayError(parent, message, "Error");
	}

	public static void displayError(Component parent, Object message, String title) {
		displayJOptionPane(parent, message, title, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
	}

	public static int displayJOptionPane(Component parent, Object message, String title, int messageType, int optionType, Icon icon) {
		final JDialog dialog = new JDialog();
		dialog.setTitle(title);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setModal(true);

		final JOptionPane optionPane = new JOptionPane(message, messageType, optionType, icon);
		optionPane.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (dialog.isVisible() && (e.getSource() == optionPane)) {
					dialog.setVisible(false);
				}
			}
		});

		dialog.setContentPane(optionPane);
		dialog.pack();

		dialog.setBounds(centralize(dialog.getBounds(), parent.getLocationOnScreen(), parent.getSize()));
		dialog.setVisible(true);

		Object value = optionPane.getValue();
		try {
			return Integer.parseInt(String.valueOf(value));
		} catch (Exception ex) {
			return -1;
		}
	}

	public static Rectangle centralize(Rectangle toCentralize, Point parentLocation, Dimension parentSize) {
		int parentCenterX = parentLocation.x + (parentSize.width / 2);
		toCentralize.x = parentCenterX - toCentralize.width / 2;

		int parentCenterY = parentLocation.y + (parentSize.height / 2);
		toCentralize.y = parentCenterY - toCentralize.height / 2;

		if (toCentralize.x < 0) {
			toCentralize.x = 0;
		}
		if (toCentralize.y < 0) {
			toCentralize.y = 0;
		}

		return toCentralize;
	}

	public static Color displayJColorChooser(Component parent, String title, Color initialColor) {
		final JColorChooser colorChooser = new JColorChooser(initialColor);
		final ColorTracker ok = new ColorTracker(colorChooser);

		final JDialog dialog = JColorChooser.createDialog(parent, title, true, colorChooser, ok, null);

		dialog.setBounds(centralize(dialog.getBounds(), parent.getLocationOnScreen(), parent.getSize()));
		dialog.setVisible(true);

		return ok.getColor();
	}

	public static JDialog createDialog(Container panel) {
		JDialog dialog = new JDialog();

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setModal(false);

		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);

		return dialog;
	}

	public static void displayTestFrame(Component component) {
		try {
			final JFrame f = new JFrame();
			f.setLayout(new BorderLayout());
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			f.add(component, BorderLayout.CENTER);
			f.setBounds(0, 0, 800, 600);
			f.setLocationRelativeTo(null);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					f.setVisible(true);
					f.repaint(100);
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

class ColorTracker implements ActionListener {
	JColorChooser chooser;
	Color color;

	public ColorTracker(JColorChooser c) {
		chooser = c;
	}

	public void actionPerformed(ActionEvent e) {
		color = chooser.getColor();
	}

	public Color getColor() {
		return color;
	}
}