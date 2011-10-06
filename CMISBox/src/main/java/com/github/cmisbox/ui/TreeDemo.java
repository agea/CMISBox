package com.github.cmisbox.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

public class TreeDemo extends BaseFrame implements TreeSelectionListener {

	private class BookInfo {
		public String bookName;
		public String bookURL;

		public BookInfo(String book, String filename) {
			this.bookName = book;
			this.bookURL = filename;
		}

		public String toString() {
			return this.bookName;
		}
	}

	private JTree tree;

	// Optionally play with line styles. Possible values are
	// "Angled" (the default), "Horizontal", and "None".
	private static boolean playWithLineStyle = false;

	private static String lineStyle = "Horizontal";

	private void createNodes(DefaultMutableTreeNode top) {
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode book = null;

		category = new DefaultMutableTreeNode("Books for Java Programmers");
		top.add(category);

		// original Tutorial
		book = new DefaultMutableTreeNode(new BookInfo(
				"The Java Tutorial: A Short Course on the Basics",
				"tutorial.html"));

		category.add(book);

		// Tutorial Continued
		book = new DefaultMutableTreeNode(new BookInfo(
				"The Java Tutorial Continued: The Rest of the JDK",
				"tutorialcont.html"));
		category.add(book);

		// JFC Swing Tutorial
		book = new DefaultMutableTreeNode(new BookInfo(
				"The JFC Swing Tutorial: A Guide to Constructing GUIs",
				"swingtutorial.html"));
		category.add(book);

		// Bloch
		book = new DefaultMutableTreeNode(new BookInfo(
				"Effective Java Programming Language Guide", "bloch.html"));
		category.add(book);

		// Arnold/Gosling
		book = new DefaultMutableTreeNode(new BookInfo(
				"The Java Programming Language", "arnold.html"));
		category.add(book);

		// Chan
		book = new DefaultMutableTreeNode(new BookInfo(
				"The Java Developers Almanac", "chan.html"));
		category.add(book);

		category = new DefaultMutableTreeNode("Books for Java Implementers");
		top.add(category);

		// VM
		book = new DefaultMutableTreeNode(new BookInfo(
				"The Java Virtual Machine Specification", "vm.html"));
		category.add(book);

		// Language Spec
		book = new DefaultMutableTreeNode(new BookInfo(
				"The Java Language Specification", "jls.html"));
		category.add(book);
	}

	@Override
	protected String getWindowTitle() {
		return "Tree";
	}

	@Override
	protected void initComponents() {
		// Create the nodes.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				"The Java Series");
		this.createNodes(top);

		// Create a tree that allows one selection at a time.
		this.tree = new JTree(top);
		this.tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		this.tree.addTreeSelectionListener(this);

		if (TreeDemo.playWithLineStyle) {
			System.out.println("line style = " + TreeDemo.lineStyle);
			this.tree.putClientProperty("JTree.lineStyle", TreeDemo.lineStyle);
		}

		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;

		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		cs.weightx = 100;
		cs.weighty = 100;

		DefaultTreeCellRenderer ctr = new DefaultTreeCellRenderer();

		ctr.setBackgroundNonSelectionColor(Color.darkGray);
		ctr.setForeground(Color.white);

		this.tree.setCellRenderer(ctr);

		this.tree.setBackground(Color.darkGray);
		this.tree.setPreferredSize(new Dimension(300, 300));

		this.mainPanel.add(this.tree, cs);

	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.tree
				.getLastSelectedPathComponent();

		if (node == null) {
			return;
		}

		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			BookInfo book = (BookInfo) nodeInfo;
			System.out.print(book.bookURL + ":  \n    ");
		}
		System.out.println(nodeInfo.toString());
	}
}
