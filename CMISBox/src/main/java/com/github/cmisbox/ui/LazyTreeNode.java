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
