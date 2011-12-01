/*  
 *	CMISBox - Synchronize and share your files with your CMIS Repository
 *
 *	Copyright (C) 2011 - Andrea Agili 
 *  
 * 	CMISBox is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  CMISBox is distributed in the hope that it will be useful,
 *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CMISBox.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.github.cmisbox.local;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.linux.JNotify_linux;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.cmisbox.core.Config;
import com.github.cmisbox.ui.UI;

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
		if (!path.startsWith(File.separator)) {
			path = File.separator + path;
		}
		this.watches.put(path, JNotify.addWatch(Config.getInstance()
				.getWatchParent() + path, this.mask, this.watchSubtree,
				new Listener()));
		this.log.info("Watching " + path);
		UI ui = UI.getInstance();
		if (ui.isAvailable()) {
			ui.addWatch(path);
		}

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
