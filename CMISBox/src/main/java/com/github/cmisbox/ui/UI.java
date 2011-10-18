package com.github.cmisbox.ui;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;

import com.github.cmisbox.core.Config;
import com.github.cmisbox.core.Main;
import com.github.cmisbox.core.Messages;

public class UI {
	public static enum Status {
		NONE, OK, KO, SYNCH;
	}

	private static final String NOTIFY_TITLE = "CMISBox";

	private static final DarkDefaultNotification DARK_DEFAULT_NOTIFICATION = new DarkDefaultNotification();

	private static final ImageIcon NOTIFY_ICON = new ImageIcon(
			UI.class.getResource("images/cmisbox-ok.png"));

	private static UI instance = new UI();

	public static UI getInstance() {
		return UI.instance;
	}

	private boolean available = false;

	private SystemTray tray;

	private Log log;

	private PopupMenu popup;

	private UI() {
		this.log = LogFactory.getLog(this.getClass());
		try {
			this.available = !GraphicsEnvironment.isHeadless();

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			if (SystemTray.isSupported()) {

				this.tray = SystemTray.getSystemTray();
				Image image = ImageIO.read(this.getClass().getResource(
						"images/cmisbox.png"));

				ActionListener exitListener = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Main.exit(0);
					}
				};

				this.popup = new PopupMenu();
				MenuItem defaultItem = new MenuItem(Messages.exit);
				defaultItem.addActionListener(exitListener);
				this.popup.add(defaultItem);

				MenuItem loginItem = new MenuItem(Messages.login);
				loginItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						new LoginDialog();
					}
				});
				this.popup.add(loginItem);

				MenuItem treeItem = new MenuItem(Messages.showTree);
				treeItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						new TreeSelect();
					}
				});

				this.popup.add(treeItem);

				final TrayIcon trayIcon = new TrayIcon(image, UI.NOTIFY_TITLE,
						this.popup);

				trayIcon.setImageAutoSize(true);

				this.tray.add(trayIcon);

				this.notify(Messages.startupComplete);

			}

		} catch (Exception e) {
			this.log.error(e);
		}
	}

	public void addWatch(String path) {
		final MenuItem mi = new MenuItem(path.replaceAll("/", ""));
		mi.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				try {
					Desktop.getDesktop().open(
							new File(Config.getInstance().getWatchParent(), mi
									.getLabel()));
				} catch (IOException e) {
					UI.this.log.error(e);
				}

			}
		});
		this.popup.add(mi);
	}

	public File getWatchFolder() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.showOpenDialog(null);
		return chooser.getSelectedFile();
	}

	public boolean isAvailable() {
		return this.available;
	}

	public void notify(String msg) {
		new NotificationBuilder().withStyle(UI.DARK_DEFAULT_NOTIFICATION)
				.withTitle(UI.NOTIFY_TITLE).withMessage(msg)
				.withIcon(UI.NOTIFY_ICON).showNotification();
	}

	public LoginDialog openRemoteLoginDialog() {
		LoginDialog ld = new LoginDialog();
		ld.waitFor();
		return ld;
	}

	public void setStatus(Status status) {
		if (!this.isAvailable()) {
			return;
		}
		try {
			if (status == Status.SYNCH) {
				this.tray.getTrayIcons()[0].setImage(ImageIO.read(this
						.getClass().getResource("images/cmisbox-synch.png")));
			}
			if (status == Status.OK) {
				this.tray.getTrayIcons()[0].setImage(ImageIO.read(this
						.getClass().getResource("images/cmisbox-ok.png")));
			}
			if (status == Status.KO) {
				this.tray.getTrayIcons()[0].setImage(ImageIO.read(this
						.getClass().getResource("images/cmisbox-error.png")));
			}
			if (status == Status.NONE) {
				this.tray.getTrayIcons()[0].setImage(ImageIO.read(this
						.getClass().getResource("images/cmisbox.png")));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
