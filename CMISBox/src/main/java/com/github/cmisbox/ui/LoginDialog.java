package com.github.cmisbox.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class LoginDialog extends BaseFrame {

	private JLabel usernameLabel;
	private JTextField usernameField;
	private JLabel passwordLabel;
	private JPasswordField passwordField;
	private JLabel loginButton;
	private JLabel cancelButton;

	public String getPassword() {
		return new String(this.passwordField.getPassword());
	}

	public String getUsername() {
		return this.usernameField.getText().trim();
	}

	@Override
	protected String getWindowTitle() {
		return "Login";
	}

	@Override
	protected void initComponents() {
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

		this.usernameField = new JTextField(20);
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

		this.passwordField = new JPasswordField(20);
		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		panel.add(this.passwordField, cs);
		panel.setBorder(new LineBorder(Color.GRAY));

		this.loginButton = new JLabel(new ImageIcon(this.getImage(
				"images/gtk-yes.png", 32, 32)));

		this.loginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// try login
			}
		});
		this.cancelButton = new JLabel(new ImageIcon(this.getImage(
				"images/gtk-no.png", 32, 32)));

		this.cancelButton.addMouseListener(this.closeAdapter);
		JPanel bp = new JPanel();
		bp.add(this.loginButton);
		bp.add(this.cancelButton);

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
