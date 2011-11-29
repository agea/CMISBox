package com.github.cmisbox.remote;

import java.util.List;

public class Changes {
	private String token;

	private List<ChangeItem> events;

	public List<ChangeItem> getEvents() {
		return this.events;
	}

	public String getToken() {
		return this.token;
	}

	public void setEvents(List<ChangeItem> items) {
		this.events = items;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return String.format("Changes(%s,%s)", this.token, this.events);
	}
}
