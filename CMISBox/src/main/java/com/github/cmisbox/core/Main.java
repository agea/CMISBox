package com.github.cmisbox.core;

import java.io.IOException;

import org.apache.commons.logging.LogFactory;

import com.github.cmisbox.local.Watcher;

public class Main {

	public static void main(String[] args) {
		new Main().start(args);
	}

	private void start(String[] args) {
		LogFactory.getLog(this.getClass()).info("Starting CMISBox...");
		Config.getInstance();
		try {
			Watcher.getInstance().addWatch(args[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
