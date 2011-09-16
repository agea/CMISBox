package com.github.cmisbox.ui;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UI {
	private static UI instance = new UI();

	public static UI getInstance() {
		return UI.instance;
	}

	private boolean available = false;

	private SystemTray tray;

	private UI() {
		Log log = LogFactory.getLog(this.getClass());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			if (SystemTray.isSupported()) {

				this.tray = SystemTray.getSystemTray();
				Image image = ImageIO.read(this.getClass().getResource(
						"icon.png"));

				ActionListener exitListener = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println("Exiting...");
						System.exit(0);
					}
				};

				PopupMenu popup = new PopupMenu();
				MenuItem defaultItem = new MenuItem("Exit");
				defaultItem.addActionListener(exitListener);
				popup.add(defaultItem);

				final TrayIcon trayIcon = new TrayIcon(image, "Tray Demo",
						popup);

				trayIcon.setImageAutoSize(true);

				this.tray.add(trayIcon);

			}
			this.available = true;
		} catch (Exception e) {
			log.error(e);
		}
	}

	public boolean isAvailable() {
		return this.available;
	}
}
