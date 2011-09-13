package com.github.cmisbox.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

public class Config {

	public static enum OS {
		LINUX, WINDOWS, MACOSX;
	}

	private static final String PROPERTIES_FILE = "cmisbox.properties";

	private File home;

	private OS os;

	private Properties properties;

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

		this.home = new File(homePath);
		if (!this.home.exists()) {
			this.home.mkdirs();
		}
		File logdir = new File(this.home, "logs");
		if (!logdir.exists()) {
			logdir.mkdirs();
		}

		System.getProperties().setProperty("cmisbox.home",
				this.home.getAbsolutePath());

		Properties logProperties = new Properties();

		try {
			logProperties.load(this.getClass().getResourceAsStream(
					"log4j.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		logProperties.setProperty("log4j.appender.R.File=cmisbox.log",
				new File(logdir, "cmisbox.log").getAbsolutePath());

		PropertyConfigurator.configure(logProperties);

		Log log = LogFactory.getLog(this.getClass());

		this.properties = this.createDefaultProperties();

		try {
			File propertiesFile = new File(this.home, Config.PROPERTIES_FILE);
			if (!propertiesFile.exists()) {
				FileOutputStream out = new FileOutputStream(propertiesFile);
				this.properties.store(out, null);
				out.close();
			}

			this.properties.load(new FileInputStream(propertiesFile));
		} catch (IOException e) {

		}

		log.info("CMISBox Home: " + this.home);

	}

	private Properties createDefaultProperties() {
		Properties p = new Properties();
		p.setProperty("bada", "chie");

		return p;
	}

	public OS getOS() {
		return this.os;
	}

}
