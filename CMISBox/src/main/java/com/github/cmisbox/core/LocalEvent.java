package com.github.cmisbox.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class LocalEvent implements Delayed {

	public static enum Type {
		CREATE, DELETE, MODIFY, RENAME;
	}

	private static final long delay = 5000;

	private List<Type> typeList = new ArrayList<Type>();

	private String rootPath;

	private String name;

	private String newName;

	private long expiration = System.currentTimeMillis() + LocalEvent.delay;

	public LocalEvent(Type type, String rootPath, String name) {
		this.typeList.add(type);
		this.rootPath = rootPath;
		this.name = name;
	}

	public LocalEvent(Type type, String rootPath, String name, String newName) {
		this.typeList.add(type);
		this.rootPath = rootPath;
		this.name = name;
		this.newName = newName;
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

	public String getName() {
		return this.name;
	}

	public String getNewName() {
		return this.newName;
	}

	public String getRootPath() {
		return this.rootPath;
	}

	public List<Type> getTypeList() {
		return this.typeList;
	}

	@Override
	public int hashCode() {
		return (this.rootPath + "/" + this.name).hashCode();
	}

	public void merge(LocalEvent queuedEvent) {
		this.typeList.addAll(0, queuedEvent.getTypeList());
		if (this.newName == null) {
			this.newName = queuedEvent.getNewName();
		}

	}

	@Override
	public String toString() {
		return this.typeList
				+ ": "
				+ this.rootPath
				+ " + "
				+ this.name
				+ (!this.typeList.contains(Type.RENAME) ? "" : " -> "
						+ this.newName);
	}

}
