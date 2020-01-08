package com.univocity.trader.chart.gui;

import javax.swing.event.*;
import java.awt.*;

public class DisplayObserver {

	private Rectangle[] screens;
	private Rectangle[] previousScreens;
	private GraphicsEnvironment env;
	private GraphicsDevice[] devices;
	private static DisplayObserver instance;
	private EventListenerList listeners;

	private final Thread screenChangeDetector = new Thread() {
		{
			this.setPriority(Thread.MIN_PRIORITY);
		}

		public void run() {
			while (true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {

				}
				refreshDisplayConfig();
				if (isDisplayEnvChanged()) {
					fireChangeDetected();
				}
				Thread.yield();
			}
		}
	};

	public static DisplayObserver getInstance() {
		if (instance == null) {
			instance = new DisplayObserver();
		}
		return instance;
	}

	private DisplayObserver() {
		refreshDisplayConfig();
		listeners = new EventListenerList();
	}

	private boolean isDisplayEnvChanged() {
		boolean changed = false;
		if (previousScreens.length == screens.length) {
			for (int i = 0; i < screens.length; i++) {
				if (!previousScreens[i].equals(screens[i])) {
					changed = true;
				}
				previousScreens[i] = screens[i];
			}
		} else {
			changed = false;
			previousScreens = screens.clone();
		}
		return changed;
	}

	private void refreshDisplayConfig() {
		env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		devices = env.getScreenDevices();

		GraphicsDevice defaultDevice = env.getDefaultScreenDevice();

		for (int i = 0; i < devices.length; i++) {
			if (devices[i] == defaultDevice) {
				GraphicsDevice tmp = devices[0];
				devices[0] = defaultDevice;
				devices[i] = tmp;
			}
		}

		if (screens == null || screens.length != devices.length) {
			screens = new Rectangle[devices.length];
			for (int i = 0; i < screens.length; i++) {
				screens[i] = new Rectangle();
			}
		}

		if (previousScreens == null) {
			previousScreens = new Rectangle[devices.length];
		}

		Toolkit toolkit = Toolkit.getDefaultToolkit();

		for (int i = 0; i < devices.length; i++) {
			Rectangle rect = devices[i].getDefaultConfiguration().getBounds();
			Rectangle insets = new Rectangle(0, 0, 0, 0);

			GraphicsConfiguration[] configs = devices[i].getConfigurations();
			for (GraphicsConfiguration config : configs) {
				Insets b = toolkit.getScreenInsets(config);
				insets = insets.union(new Rectangle(b.left, b.top, b.right, b.bottom));
			}

			rect.x += insets.x;
			rect.y += insets.y;
			rect.width -= insets.width;
			rect.height -= insets.height;

			if (previousScreens.length == screens.length) {
				previousScreens[i] = screens[i];
			}
			screens[i] = rect;
		}
	}

	public void addDisplayListener(DisplayListener listener) {
		screenChangeDetector.start();
		listeners.add(DisplayListener.class, listener);
	}

	public void removeDisplayListener(DisplayListener listener) {
		listeners.remove(DisplayListener.class, listener);
	}

	public Rectangle[] getScreenSizes() {
		return screens.clone();
	}

	private void fireChangeDetected() {
		DisplayListener[] displayListeners = listeners.getListeners(DisplayListener.class);
		for (DisplayListener listener : displayListeners) {
			listener.displayChanged();
		}
	}
}
