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