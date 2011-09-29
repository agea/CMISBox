package com.github.cmisbox.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class LoginDialogExample extends JDialog {

	private static final long serialVersionUID = 8865326084853486201L;

	private JTextField usernameField;
	private JPasswordField passwordField;
	private JTextField urlField;
	private JLabel usernameLabel;
	private JLabel passwordLabel;
	private JLabel urlLabel;
	private JButton loginButton;
	private JButton cancelButton;
	private boolean succeeded;

	public LoginDialogExample() {
		super((Frame) null, "Login", true);
		//
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;

		this.usernameLabel = new JLabel("Username: ");
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

		this.loginButton = new JButton("Login");

		this.loginButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (true) {
					JOptionPane.showMessageDialog(LoginDialogExample.this, "Hi "
							+ LoginDialogExample.this.getUsername()
							+ "! You have successfully logged in.", "Login",
							JOptionPane.INFORMATION_MESSAGE);
					LoginDialogExample.this.succeeded = true;
					LoginDialogExample.this.dispose();
				} else {
					JOptionPane.showMessageDialog(LoginDialogExample.this,
							"Invalid username or password", "Login",
							JOptionPane.ERROR_MESSAGE);
					// reset username and password
					LoginDialogExample.this.usernameField.setText("");
					LoginDialogExample.this.passwordField.setText("");
					LoginDialogExample.this.succeeded = false;

				}
			}
		});
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				LoginDialogExample.this.dispose();
			}
		});
		JPanel bp = new JPanel();
		bp.add(this.loginButton);
		bp.add(this.cancelButton);

		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.getContentPane().add(bp, BorderLayout.PAGE_END);

		this.pack();
		this.setResizable(false);

		this.setVisible(true);

	}

	public String getPassword() {
		return new String(this.passwordField.getPassword());
	}

	public String getUsername() {
		return this.usernameField.getText().trim();
	}

	public boolean isSucceeded() {
		return this.succeeded;
	}
}