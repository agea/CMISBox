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
