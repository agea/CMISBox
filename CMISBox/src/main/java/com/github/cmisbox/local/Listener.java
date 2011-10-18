package com.github.cmisbox.local;

import net.contentobjects.jnotify.JNotifyListener;

import com.github.cmisbox.core.LocalEvent;
import com.github.cmisbox.core.Queue;

class Listener implements JNotifyListener {

	private Queue queue = Queue.getInstance();

	public void fileCreated(int wd, String rootPath, String name) {
		this.queue
				.add(new LocalEvent(true, false, false, false, rootPath, name));
	}

	public void fileDeleted(int wd, String rootPath, String name) {
		this.queue
				.add(new LocalEvent(false, false, true, false, rootPath, name));
	}

	public void fileModified(int wd, String rootPath, String name) {
		this.queue
				.add(new LocalEvent(false, true, false, false, rootPath, name));
	}

	public void fileRenamed(int wd, String rootPath, String oldName,
			String newName) {
		this.queue.add(new LocalEvent(false, false, false, true, rootPath,
				oldName, newName));
	}
}