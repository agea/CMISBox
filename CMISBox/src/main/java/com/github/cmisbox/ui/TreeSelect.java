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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import net.java.balloontip.BalloonTip;

import com.github.cmisbox.core.Messages;
import com.github.cmisbox.persistence.Storage;
import com.github.cmisbox.remote.CMISRepository;

public class TreeSelect extends BaseFrame implements TreeSelectionListener {

	private static final long serialVersionUID = 3948501371857218423L;

	private JTree tree;

	private LazyTreeNode selection;

	private JTextField selText;

	private BalloonTip tip;

	public LazyTreeNode getSelection() {
		return this.selection;
	}

	@Override
	protected String getWindowTitle() {
		return "Tree";
	}

	@Override
	protected void initComponents() {
		// Create the nodes.
		LazyTreeNode top = new LazyTreeNode("ROOT", null);
		for (Entry<String, String> e : CMISRepository.getInstance().getRoots()
				.entrySet()) {
			top.add(new LazyTreeNode(e.getValue(), e.getKey()));
		}

		// Create a tree that allows one selection at a time.
		this.tree = new JTree();
		LazyTreeModel lazyTreeModel = new LazyTreeModel(top, this.tree);

		this.tree.setModel(lazyTreeModel);
		this.tree.setShowsRootHandles(true);
		this.tree.setRootVisible(false);
		this.tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		this.tree.addTreeSelectionListener(this);
		this.tree.addTreeWillExpandListener(lazyTreeModel);

		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;

		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		cs.weightx = 1000;
		cs.weighty = 100;

		DefaultTreeCellRenderer ctr = new DefaultTreeCellRenderer();

		ctr.setBackgroundNonSelectionColor(Color.darkGray);
		ctr.setForeground(Color.white);

		ctr.setTextNonSelectionColor(Color.white);

		this.tree.setCellRenderer(ctr);

		this.tree.setBackground(Color.darkGray);
		JScrollPane treeView = new JScrollPane(this.tree);
		treeView.setPreferredSize(new Dimension(350, 200));
		this.mainPanel.add(treeView, cs);

		cs.gridx = 1;
		cs.gridy = 2;
		cs.gridwidth = 1;
		cs.weightx = 1000;
		cs.weighty = 1;

		this.selText = new JTextField();
		this.selText.setEditable(true);
		this.tip = new BalloonTip(this.selText, Messages.youCanChangeFolderName);
		this.tip.setVisible(false);

		this.mainPanel.add(this.selText, cs);

		JLabel okButton = new JLabel(new ImageIcon(this.getImage(
				"images/gtk-yes.png", 32, 32)));

		okButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					Storage.getInstance().synchRemoteFolder(
							TreeSelect.this.selection.getId(),
							TreeSelect.this.selText.getText());
					new CloseAdapter(TreeSelect.this).mouseClicked(null);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(TreeSelect.this, e);
					UI.getInstance().setStatus(UI.Status.KO);
					TreeSelect.this.log.error(e);
				}
			}
		});

		cs.gridx = 2;
		cs.gridy = 2;
		cs.gridwidth = 1;
		cs.weightx = 1;
		cs.weighty = 1;

		this.mainPanel.add(okButton, cs);

	}

	public void setSelection(LazyTreeNode selection) {
		this.selection = selection;
		this.selText.setText(selection.getUserObject().toString());
		this.tip.setVisible(true);
	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e) {
		this.setSelection((LazyTreeNode) this.tree
				.getLastSelectedPathComponent());

	}
}
