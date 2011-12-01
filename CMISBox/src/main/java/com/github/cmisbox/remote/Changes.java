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
