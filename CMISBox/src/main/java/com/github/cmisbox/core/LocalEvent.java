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

package com.github.cmisbox.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class LocalEvent implements Delayed {

	private static final long delay = 5000;

	public static LocalEvent createSynchEvent() {
		LocalEvent le = new LocalEvent(false, false, false, false, null, null);
		le.synch = true;
		le.expiration = System.currentTimeMillis();
		return le;
	}

	private boolean create = false;

	private boolean delete = false;

	private boolean modify = false;

	private boolean rename = false;

	private boolean synch = false;

	private String rootPath;

	private String name;

	private String newName;

	private long expiration = System.currentTimeMillis() + LocalEvent.delay;

	public LocalEvent(boolean create, boolean modify, boolean delete,
			boolean rename, String rootPath, String name) {
		this.create = create;
		this.modify = modify;
		this.delete = delete;
		this.rename = rename;
		this.rootPath = rootPath;
		this.name = name;
		if ((this.name != null) && this.name.endsWith("/")) {
			this.name = this.name.substring(0, name.length() - 1);
		}
		if (this.isDelete()) {
			this.expiration = System.currentTimeMillis()
					+ (LocalEvent.delay / 10);
		}
	}

	public LocalEvent(boolean create, boolean modify, boolean delete,
			boolean rename, String rootPath, String name, String newName) {
		this.create = create;
		this.modify = modify;
		this.delete = delete;
		this.rename = rename;
		this.rootPath = rootPath;
		this.name = name;
		this.newName = newName;
		if ((this.name != null) && this.name.endsWith("/")) {
			this.name = this.name.substring(0, name.length() - 1);
		}
		if ((this.newName != null) && this.newName.endsWith("/")) {
			this.newName = this.newName.substring(0, name.length() - 1);
		}
		if (this.isDelete()) {
			this.expiration = System.currentTimeMillis()
					+ (LocalEvent.delay / 10);
		}

	}

	public int compareTo(Delayed o) {
		return new Long(this.getDelay(TimeUnit.SECONDS)).compareTo(new Long(o
				.getDelay(TimeUnit.SECONDS)));
	}

	@Override
	public boolean equals(Object obj) {
		try {
			LocalEvent other = (LocalEvent) obj;
			return other.hashCode() == this.hashCode();
		} catch (Exception e) {
			return false;
		}
	}

	public long getDelay(TimeUnit unit) {
		return unit.convert(this.expiration - System.currentTimeMillis(),
				TimeUnit.MILLISECONDS);
	}

	public String getEffectiveName() {
		return this.newName != null ? this.newName : this.name;
	}

	public List<String> getEvents() {
		ArrayList<String> l = new ArrayList<String>();
		if (this.create) {
			l.add("CREATE");
		}
		if (this.modify) {
			l.add("MODIFY");
		}
		if (this.rename) {
			l.add("RENAME");
		}
		if (this.delete) {
			l.add("DELETE");
		}
		return l;
	}

	public String getFilename() {
		String n = this.newName != null ? this.newName : this.name;

		if (n == null) {
			return null;
		}

		String[] s = n.split(File.separator);
		return s[s.length - 1];
	}

	public String getFullFilename() {
		return this.rootPath + File.separator
				+ (this.newName != null ? this.newName : this.name);
	}

	public String getLocalNewPath() {
		return this.getFullFilename().substring(
				Config.getInstance().getWatchParent().length());
	}

	public String getLocalPath() {
		return (this.rootPath + File.separator + this.name).substring(Config
				.getInstance().getWatchParent().length());
	}

	public String getName() {
		return this.name;
	}

	public String getNewName() {
		return this.newName;
	}

	public String getRootPath() {
		return this.rootPath;
	}

	@Override
	public int hashCode() {
		return (this.rootPath + "/" + this.name).hashCode();
	}

	public boolean isCreate() {
		return this.create;
	}

	public boolean isDelete() {
		return this.delete
				|| ((Config.getInstance().getOS() == Config.OS.LINUX)
						&& this.isRename() && (this.newName == null));
	}

	public boolean isEffectiveRename() {
		return this.isRename() && (this.name != null) && (this.newName != null);
	}

	public boolean isModify() {
		return this.modify || this.isCreate()
				|| (this.isRename() && (this.name == null));
	}

	public boolean isParent(LocalEvent queuedEvent) {
		return queuedEvent.getLocalPath().startsWith(this.getLocalPath());
	}

	public boolean isRename() {
		return this.rename;
	}

	public boolean isSynch() {
		return this.synch;
	}

	public void merge(LocalEvent queuedEvent) {
		this.create = queuedEvent.isCreate() || this.create;
		this.delete = queuedEvent.isDelete() || this.delete;
		this.modify = queuedEvent.isModify() || this.modify;
		this.rename = queuedEvent.isRename() || this.rename;

		if (this.newName == null) {
			this.newName = queuedEvent.getNewName();
		}

	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
	}

	public void setRename(boolean rename) {
		this.rename = rename;
	}

	@Override
	public String toString() {
		return this.getEvents() + ": " + this.rootPath + " + " + this.name
				+ (!this.rename ? "" : " -> " + this.newName);
	}

}
