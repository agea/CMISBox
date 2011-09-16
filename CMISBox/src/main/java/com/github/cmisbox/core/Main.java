package com.github.cmisbox.core;

import org.apache.commons.logging.LogFactory;

import com.github.cmisbox.ui.UI;

public class Main {

	public static void main(String[] args) {
		new Main().start(args);
	}

	private void start(String[] args) {
		LogFactory.getLog(this.getClass()).info("Starting CMISBox...");
		Config config = Config.getInstance();
		UI.getInstance();

	}

}
