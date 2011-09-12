package com.github.cmisbox.core;

import java.util.Iterator;
import java.util.concurrent.DelayQueue;

import org.apache.commons.logging.LogFactory;

public class Queue implements Runnable {

	private static Queue instance = new Queue();

	public static Queue getInstance() {
		return Queue.instance;
	}

	private boolean active = true;

	private Thread thread;

	private DelayQueue<LocalEvent> delayQueue = new DelayQueue<LocalEvent>();

	private Queue() {
		this.thread = new Thread(this, "Queue");
		this.thread.start();
	}

	public synchronized void add(LocalEvent localEvent) {
		LogFactory.getLog(this.getClass())
				.debug("watched event: " + localEvent);
		if (!this.active) {
			return;
		}
		if (this.delayQueue.contains(localEvent)) {
			Iterator<LocalEvent> i = this.delayQueue.iterator();
			while (i.hasNext()) {
				LocalEvent queuedEvent = i.next();
				if (queuedEvent.equals(localEvent)) {
					localEvent.merge(queuedEvent);
					i.remove();
				}
			}
		}
		this.delayQueue.put(localEvent);
		LogFactory.getLog(this.getClass()).debug("queued: " + localEvent);
	}

	public void manageEvent(LocalEvent event) {
		LogFactory.getLog(this.getClass()).debug("managing: " + event);
	}

	public void run() {
		while (this.active) {
			try {
				this.manageEvent(this.delayQueue.take());
			} catch (InterruptedException e) {
				LogFactory.getLog(this.getClass()).info(this, e);
			}
		}

	}

	public void stop() {
		this.active = false;
		this.delayQueue.clear();
		this.thread.interrupt();
	}

}
