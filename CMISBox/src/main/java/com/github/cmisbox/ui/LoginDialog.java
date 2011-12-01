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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.github.cmisbox.core.Config;
import com.github.cmisbox.core.Messages;
import com.github.cmisbox.remote.CMISRepository;

public class LoginDialog extends BaseFrame {

	private static final class LoginAdapter extends MouseAdapter {
		private CloseAdapter closeAdapter;
		private LoginDialog ld;

		public LoginAdapter(LoginDialog ld, CloseAdapter closeAdapter) {
			this.ld = ld;
			this.closeAdapter = closeAdapter;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// this.ld.loginLabel.setText(Messages.connecting + "...");
			// this.ld.loginLabel.repaint();

			try {

				Config.getInstance().setCredentials(this.ld.getUsername(),
						this.ld.getPassword(), this.ld.getUrl());
				CMISRepository.doLogin();
				this.closeAdapter.mouseClicked(null);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this.ld, "Login failed!: " + e1,
						"Login", JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	private static final long serialVersionUID = 5802095962633591722L;

	private JLabel usernameLabel;
	private JTextField usernameField;
	private JLabel passwordLabel;
	private JPasswordField passwordField;
	private JLabel urlLabel;
	private JTextField urlField;
	private JLabel loginButton;
	private JLabel loginLabel;

	public String getPassword() {
		return new String(this.passwordField.getPassword());
	}

	public String getUrl() {
		return this.urlField.getText();
	}

	public String getUsername() {
		return this.usernameField.getText().trim();
	}

	@Override
	protected String getWindowTitle() {
		return Messages.login;
	}

	@Override
	protected void initComponents() {
		Config config = Config.getInstance();
		// this.setPreferredSize(new Dimension(400, 400));
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;

		this.usernameLabel = new JLabel("Username: ");
		this.usernameLabel.setForeground(Color.white);
		cs.gridx = 0;
		cs.gridy = 0;
		cs.gridwidth = 1;
		panel.add(this.usernameLabel, cs);

		this.usernameField = new JTextField(30);
		this.usernameField.setText(config.getRepositoryUsername());
		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridwidth = 2;
		panel.add(this.usernameField, cs);

		this.passwordLabel = new JLabel("Password: ");
		this.passwordLabel.setForeground(Color.white);
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		panel.add(this.passwordLabel, cs);

		this.passwordField = new JPasswordField(30);
		this.passwordField.setText(config.getRepositoryPassword());
		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		panel.add(this.passwordField, cs);

		this.urlLabel = new JLabel("Repository url: ");
		this.urlLabel.setForeground(Color.white);
		cs.gridx = 0;
		cs.gridy = 2;
		cs.gridwidth = 1;
		panel.add(this.urlLabel, cs);

		this.urlField = new JTextField(30);
		this.urlField.setText(config.getRepositoryUrl() != null ? config
				.getRepositoryUrl() : "http://localhost:8080/alfresco/s/cmis");
		cs.gridx = 1;
		cs.gridy = 2;
		cs.gridwidth = 2;
		panel.add(this.urlField, cs);

		panel.setBorder(new LineBorder(Color.GRAY));

		this.loginButton = new JLabel(new ImageIcon(this.getImage(
				"images/gtk-yes.png", null, null)));

		this.loginButton.addMouseListener(new LoginAdapter(this,
				this.closeAdapter));
		this.loginLabel = new JLabel("Click lo login: ");
		this.loginLabel.setForeground(Color.white);
		JPanel bp = new JPanel();
		bp.add(this.loginLabel);
		bp.add(this.loginButton);

		cs.gridx = 1;
		cs.gridy = 1;
		cs.weightx = 100;
		cs.weighty = 100;

		this.mainPanel.add(panel, cs);

		cs.gridx = 1;
		cs.gridy = 2;
		cs.weightx = 100;
		cs.weighty = 0;
		this.mainPanel.add(bp, cs);

	}

}
