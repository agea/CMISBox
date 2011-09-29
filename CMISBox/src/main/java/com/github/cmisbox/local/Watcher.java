package com.github.cmisbox.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.linux.JNotify_linux;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Watcher implements Runnable {

	public static Watcher getInstance() {
		return Watcher.instance;
	}

	private int mask = JNotify.FILE_ANY | JNotify_linux.IN_ALL_EVENT;

	private boolean watchSubtree = true;
	private final Map<String, Integer> watches = new HashMap<String, Integer>();

	private static Watcher instance = new Watcher();

	private Thread thread;

	private Log log;

	private Watcher() {
		this.log = LogFactory.getLog(this.getClass());

		this.thread = new Thread(this, "Watcher");
		this.thread.start();
	}

	public void addWatch(String path) throws IOException {
		this.watches.put(path, JNotify.addWatch(path, this.mask,
				this.watchSubtree, new Listener()));
		this.log.info("Watching " + path);

	}

	public void removeWatch(String path) throws IOException {
		if (this.watches.containsKey(path)) {
			JNotify.removeWatch(this.watches.get(path));
		}
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(100000);
			}
		} catch (InterruptedException e) {
			for (Integer id : Watcher.this.watches.values()) {
				try {
					JNotify.removeWatch(id);
				} catch (JNotifyException e1) {
					LogFactory.getLog(this.getClass()).error(this, e);
				}
			}
		}
	}

	public void stop() {
		this.thread.interrupt();
	}

}
