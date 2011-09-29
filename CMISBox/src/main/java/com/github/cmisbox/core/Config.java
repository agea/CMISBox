package com.github.cmisbox.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.github.cmisbox.ui.UI;

public class Config {

	public static enum OS {
		LINUX, WINDOWS, MACOSX;
	}

	private static final String WATCHPARENT = "watchparent";

	private static final String PROPERTIES_FILE = "cmisbox.properties";

	private File configHome;

	private OS os;

	private Properties properties;

	private Log log;

	private static Config instance = new Config();

	public static Config getInstance() {
		return Config.instance;
	}

	private Config() {

		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.equals("linux")) {
			this.os = OS.LINUX;
		} else if (osName.startsWith("windows")) {
			this.os = OS.WINDOWS;
		} else if (osName.startsWith("mac os x")) {
			this.os = OS.MACOSX;
		} else {
			throw new RuntimeException("Unsupported OS : " + osName);
		}

		String homePath = System.getProperty("user.home");
		if (this.os == OS.LINUX) {
			homePath += "/.cmisbox";
		} else if (this.os == OS.MACOSX) {
			homePath += "/Library/Application Support/CMISBox";
		} else if (this.os == OS.WINDOWS) {
			homePath = System.getenv("APPDATA") + "/CMISBox";
		}

		this.configHome = new File(homePath);
		if (!this.configHome.exists()) {
			this.configHome.mkdirs();
		}
		File logdir = new File(this.configHome, "logs");
		if (!logdir.exists()) {
			logdir.mkdirs();
		}

		System.getProperties().setProperty("cmisbox.home",
				this.configHome.getAbsolutePath());

		PropertyConfigurator.configure(this.getClass().getResource(
				"log4j.properties"));
		this.log = LogFactory.getLog(this.getClass());

		this.properties = this.createDefaultProperties();

		try {
			File propertiesFile = new File(this.configHome,
					Config.PROPERTIES_FILE);
			if (!propertiesFile.exists()) {
				FileOutputStream out = new FileOutputStream(propertiesFile);
				this.properties.store(out, null);
				out.close();
			}

			this.properties.load(new FileInputStream(propertiesFile));

		} catch (IOException e) {

		}

		this.log.info("CMISBox config home: " + this.configHome);

		String watchParent = null;

		UI ui = UI.getInstance();
		while (watchParent == null) {
			if (ui.isAvailable()) {
				File f = ui.getWatchFolder();
				watchParent = f != null ? f.getAbsolutePath() : null;

				if (watchParent == null) {

				}
			} else {
				System.err
						.print("Unable to locate watch parent folder, please insert one in cmisbox.properties");
				this.log.error("Unable to locate watch parent folder");
				Main.exit(1);
			}
		}

	}

	private Properties createDefaultProperties() {
		Properties p = new Properties();

		return p;
	}

	public OS getOS() {
		return this.os;
	}

	public String getRepositoryPassword() {
		return this.properties.getProperty("repository.password");
	}

	public String getRepositoryUrl() {
		return this.properties.getProperty("repository.url");
	}

	public String getRepositoryUsername() {
		return this.properties.getProperty("repository.username");
	}

	public String getWatchParent() {
		return this.properties.getProperty(Config.WATCHPARENT);
	}

	private void saveProperties() {
		File f = new File(this.configHome, Config.PROPERTIES_FILE);

		try {
			FileOutputStream fos = new FileOutputStream(f);
			this.properties.store(fos, null);
		} catch (Exception e) {
			this.log.error(e);
			UI ui = UI.getInstance();
			if (ui.isAvailable()) {
				ui.notify(e.getLocalizedMessage());
			}
		}

	}

	public void setWatchParent(String watchParent) {
		this.properties.setProperty(Config.WATCHPARENT, watchParent);
		this.saveProperties();
	}

}
