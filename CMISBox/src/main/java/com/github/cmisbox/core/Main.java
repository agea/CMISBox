package com.github.cmisbox.core;

import org.apache.commons.logging.LogFactory;

import com.github.cmisbox.local.Watcher;
import com.github.cmisbox.ui.UI;

public class Main {

	public static void exit(int code) {
		LogFactory.getLog(Main.class).info("Exiting CMISBox...");
		System.exit(code);
	}

	public static void main(String[] args) {
		Config.getInstance();
		LogFactory.getLog(Main.class).info("Starting CMISBox...");
		UI.getInstance();
		Watcher.getInstance();

	}

}
