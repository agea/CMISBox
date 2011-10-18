package com.github.cmisbox.core;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.cmisbox.persistence.Storage;
import com.github.cmisbox.persistence.StoredItem;
import com.github.cmisbox.remote.CMISRepository;
import com.github.cmisbox.ui.UI;

public class Queue implements Runnable {

	private static Queue instance = new Queue();

	public static Queue getInstance() {
		return Queue.instance;
	}

	private boolean active = true;

	// by default do not synch files starting with a dot
	private Pattern filter = Pattern.compile("$\\..*");

	private Thread thread;

	private DelayQueue<LocalEvent> delayQueue = new DelayQueue<LocalEvent>();

	private Log log;

	private Queue() {
		this.thread = new Thread(this, "Queue");
		this.thread.start();
		this.log = LogFactory.getLog(this.getClass());
	}

	public synchronized void add(LocalEvent localEvent) {
		this.log.debug(localEvent);
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
		if (!(localEvent.isCreate() && localEvent.isDelete())) {
			this.delayQueue.put(localEvent);
		}
	}

	public Pattern getFilter() {
		return this.filter;
	}

	public void manageEvent(LocalEvent event) {
		Log log = LogFactory.getLog(this.getClass());
		log.debug("managing: " + event);

		// any platform
		// - a folder can be renamed before containing files are managed: on
		// folder rename all children must be updated while still in queue;

		// linux
		// - if a file or folder is moved out of a watched folder it is reported
		// as a rename to null (check if it's still there)

		// mac osx
		// - recursive folder operations (e.g. unzip an archive or move a folder
		// inside a watched folder) are not reported, only root folder is
		// reported as create
		// - folder rename causes children to be notified as deleted (with old
		// path)

		try {
			File f = new File(event.getFullFilename());
			if (event.isCreate()) {
				String parent = f.getParent();
				List<StoredItem> pl = Storage.getInstance().findByPath(parent);
				if (pl.size() == 1) {
					StoredItem pi = pl.get(0);
					CMISRepository.getInstance().addChild(pi.getId(), f);

				} else {
					throw new Exception("Wrong parent: " + parent + ": " + pl);
				}

			} else if (event.isDelete()) {

			} else if (event.isModify()) {

			}

		} catch (Exception e) {
			log.error(e);
			if (UI.getInstance().isAvailable()) {
				UI.getInstance().notify(e.toString());
			}
		}
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

	public void setFilter(Pattern filter) {
		this.filter = filter;
	}

	public void stop() {
		this.active = false;
		this.delayQueue.clear();
		this.thread.interrupt();
	}

}
