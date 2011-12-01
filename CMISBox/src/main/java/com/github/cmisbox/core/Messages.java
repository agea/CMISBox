/*  
 *	CMISBox - Synchronize and share your files with your CMIS Repository
 *
 *	Copyright (C) 2011 - Andrea Agili 
 *  
 * 	CMISBox is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  CMISBox is distributed in the hope that it will be useful,
 *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CMISBox.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

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
	public static String synchComplete;
	public static String isSynchronized;
	public static String conflict;
	public static String updated;
	public static String updatedBy;
	public static String files;
	public static String renameError;
	public static String downloading;
	public static String errorDownloading;

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
