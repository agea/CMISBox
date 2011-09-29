package com.github.cmisbox.ui;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;

import com.github.cmisbox.core.Main;

public class UI {
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

				PopupMenu popup = new PopupMenu();
				MenuItem defaultItem = new MenuItem("Exit");
				defaultItem.addActionListener(exitListener);
				popup.add(defaultItem);

				final TrayIcon trayIcon = new TrayIcon(image, UI.NOTIFY_TITLE,
						popup);

				trayIcon.setImageAutoSize(true);

				this.tray.add(trayIcon);

				String msg = "Startup complete";
				this.notify(msg);

			}

		} catch (Exception e) {
			this.log.error(e);
		}
	}

	public File getWatchFolder() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choose your box");
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

	public void openRemoteLoginDialog() {
		LoginDialog ld = new LoginDialog();
		ld.waitFor();
	}
}
