package com.github.cmisbox.core;

import java.lang.reflect.Field;
import java.util.ResourceBundle;

public class Messages {

	public static String unsupportedOs;
	public static String cmisBoxConfigHome;
	public static String unableToLocateParent;
	public static String pleaseInsertInProps;
	public static String exitingCmisBox;
	public static String startingCmisBox;
	public static String provideCredentialsInProps;
	public static String startupComplete;
	public static String exit;
	public static String connecting;
	public static String login;
	public static String showTree;
	public static String synchAlreadyExisting;
	public static String createdIndexFolder;
	public static String folder;
	public static String youCanChangeFolderName;

	public static void init() {
		ResourceBundle rb = ResourceBundle
				.getBundle("com.github.cmisbox.core.messages");

		for (Field f : Messages.class.getFields()) {
			try {
				f.set(null, rb.getString(f.getName()));
			} catch (Exception e) {
			}
		}

	}
}
