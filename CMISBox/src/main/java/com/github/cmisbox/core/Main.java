package com.github.cmisbox.core;

import org.apache.commons.logging.LogFactory;

import com.github.cmisbox.local.Watcher;
import com.github.cmisbox.persistence.Storage;
import com.github.cmisbox.remote.CMISRepository;
import com.github.cmisbox.ui.UI;

public class Main {

	public static void exit(int code) {
		Storage.getInstance().close();
		LogFactory.getLog(Main.class).info(Messages.exitingCmisBox + "...");
		System.exit(code);
	}

	public static void main(String[] args) {
		Messages.init();
		Config.getInstance();
		LogFactory.getLog(Main.class).info(Messages.startingCmisBox + "...");
		UI.getInstance();
		Watcher.getInstance();
		Storage.getInstance();
		CMISRepository.getInstance();

	}

}
