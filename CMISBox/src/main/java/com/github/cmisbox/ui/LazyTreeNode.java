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

package com.github.cmisbox.ui;

import javax.swing.tree.DefaultMutableTreeNode;

public class LazyTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 7903290459254611320L;
	private boolean loaded;
	private String id;

	public LazyTreeNode(String id, Object userObject) {
		super(userObject, true);
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	@Override
	public boolean isLeaf() {
		return !this.getAllowsChildren();
	}

	protected boolean isLoaded() {
		return this.loaded;
	}

	protected void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
}
