package com.github.cmisbox.core;

import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.regex.Pattern;

import org.apache.commons.logging.LogFactory;

public class Queue implements Runnable {

	private static Queue instance = new Queue();

	public static Queue getInstance() {
		return Queue.instance;
	}

	private boolean active = true;

	private Pattern filter;

	private Thread thread;

	private DelayQueue<LocalEvent> delayQueue = new DelayQueue<LocalEvent>();

	private Queue() {
		this.thread = new Thread(this, "Queue");
		this.thread.start();
	}

	public synchronized void add(LocalEvent localEvent) {
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
	}

	public void manageEvent(LocalEvent event) {
		LogFactory.getLog(this.getClass()).debug("managing: " + event);
		// linux
		// - if a file or folder is moved out of a watched folder it is reported
		// as a rename to null
		// mac osx
		// - recursive folder operations (e.g. unzip an archive or move a folder
		// inside a watched folder) are not reported, only root folder is
		// reported as create
		// - folder rename causes children to be notified as deleted (with old
		// path)
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
