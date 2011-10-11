package com.github.cmisbox.ui;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import com.github.cmisbox.remote.CMISRepository;

public class LazyTreeModel extends DefaultTreeModel implements
		TreeWillExpandListener {

	class LoadNodesWorker extends Thread {

		private LazyTreeNode parentNode;

		LoadNodesWorker(LazyTreeNode parent) {
			this.parentNode = parent;
		}

		public void run() {
			final LazyTreeNode[] treeNodes = LazyTreeModel.this
					.loadChildren(this.parentNode);
			if (treeNodes == null) {
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					LoadNodesWorker.this.parentNode.setLoaded(true);
					LazyTreeModel.this.setChildren(
							LoadNodesWorker.this.parentNode, treeNodes);
				}
			});
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4860297919675952275L;

	public LazyTreeModel(TreeNode root, JTree tree) {
		super(root);
		this.setAsksAllowsChildren(true);
		tree.addTreeWillExpandListener(this);
		tree.setModel(this);
	}

	protected LazyTreeNode createLoadingNode() {
		return new LazyTreeNode(null, "Loading...");
	}

	protected LazyTreeNode createReloadingNode() {
		return new LazyTreeNode(null, "Refreshing...");
	}

	private LazyTreeNode findNode(String id) {
		return this.findNode(id, (LazyTreeNode) this.getRoot());
	}

	private LazyTreeNode findNode(String id, LazyTreeNode parent) {
		int count = parent.getChildCount();
		for (int i = 0; i < count; i++) {
			LazyTreeNode node = (LazyTreeNode) parent.getChildAt(i);
			if (id.equals(node.getId())) {
				return node;
			}
			if (node.isLoaded()) {
				node = this.findNode(id, node);
				if (node != null) {
					return node;
				}
			}
		}
		return null;
	}

	public LazyTreeNode findParent(String id) {
		LazyTreeNode node = this.findNode(id);
		if ((node != null) && (node.getParent() != null)) {
			return (LazyTreeNode) node.getParent();
		}
		return null;
	}

	public LazyTreeNode[] loadChildren(LazyTreeNode parentNode) {
		String id = parentNode.getId();
		TreeMap<String, String> res;
		if (!"ROOT".equals(id)) {
			res = CMISRepository.getInstance().getChildrenFolders(id);
		} else {
			res = CMISRepository.getInstance().getRoots();
		}
		ArrayList<LazyTreeNode> children = new ArrayList<LazyTreeNode>();
		for (Entry<String, String> e : res.entrySet()) {
			children.add(new LazyTreeNode(e.getValue(), e.getKey()));
		}
		return children.toArray(new LazyTreeNode[] {});
	}

	public void loadFirstLevel() {
		this.setLoading((LazyTreeNode) this.getRoot(), false);

		new LoadNodesWorker((LazyTreeNode) this.getRoot()).start();
	}

	public void reloadNode(String id) {
		LazyTreeNode node = this.findNode(id);
		if (node != null) {
			node.setLoaded(false);
			this.setLoading(node, true);
			new LoadNodesWorker(node).start();
		}
	}

	public void reloadParentNode(String id) {
		LazyTreeNode node = this.findParent(id);
		if (node != null) {
			node.setLoaded(false);
			this.setLoading(node, true);
			new LoadNodesWorker(node).start();
		}
	}

	protected void setChildren(LazyTreeNode parentNode, LazyTreeNode... nodes) {
		if (nodes == null) {
			return;
		}
		int childCount = parentNode.getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				this.removeNodeFromParent((MutableTreeNode) parentNode
						.getChildAt(0));
			}
		}
		for (int i = 0; i < nodes.length; i++) {
			this.insertNodeInto(nodes[i], parentNode, i);
		}
	}

	private void setLoading(final LazyTreeNode parentNode, final boolean reload) {
		if (SwingUtilities.isEventDispatchThread()) {
			this.setLoading2(parentNode, reload);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						LazyTreeModel.this.setLoading2(parentNode, reload);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void setLoading2(final LazyTreeNode parentNode, final boolean reload) {
		if (reload) {
			this.setChildren(parentNode, this.createReloadingNode());
		} else {
			this.setChildren(parentNode, this.createLoadingNode());
		}
	}

	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {
	}

	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException {
		LazyTreeNode node = (LazyTreeNode) event.getPath()
				.getLastPathComponent();
		if (node.isLoaded()) {
			return;
		}
		this.setLoading(node, false);
		new LoadNodesWorker(node).start();
	}
}