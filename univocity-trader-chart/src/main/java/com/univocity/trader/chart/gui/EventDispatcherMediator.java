package com.univocity.trader.chart.gui;

import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;

public class EventDispatcherMediator {
	private static final Logger log = LoggerFactory.getLogger(EventDispatcherMediator.class);
	private int dispatchingInterval;
	private boolean waitMore;
	private final Set<EventDispatcher> pendingEvents = ConcurrentHashMap.newKeySet();

	public EventDispatcherMediator() {
		this(100);
	}

	public EventDispatcherMediator(int dispatchingInterval) {
		this.dispatchingInterval = dispatchingInterval;
		this.waitMore = false;

		Thread dispatcherThread = new Thread(() -> {
			while (true) {
				try {
					synchronized (pendingEvents) {
						pendingEvents.wait(dispatchingInterval);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				if (!waitMore) {
					Iterator<EventDispatcher> iterator = pendingEvents.iterator();
					while (iterator.hasNext()) {
						EventDispatcher dispatcher = iterator.next();
						if (dispatcher != null) {
							try {
								dispatcher.dispatchEvent();
							} catch (Exception ex) {
								log.error("error", ex);
							}
						}
						iterator.remove();
					}
				}
				waitMore = false;
			}
		});
		dispatcherThread.setDaemon(true);
		dispatcherThread.setPriority(Thread.MIN_PRIORITY);
		dispatcherThread.start();
	}

	public void dispatchEvent(EventDispatcher dispatcher) {
		waitMore = true;
		pendingEvents.add(dispatcher);
		synchronized (pendingEvents){
			pendingEvents.notify();
		}
	}

	public int getDispatchingInterval() {
		return dispatchingInterval;
	}

	public void setDispatchingInterval(int dispatchingInterval) {
		this.dispatchingInterval = dispatchingInterval;
	}
}
