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

package com.github.cmisbox.remote;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;

import com.github.cmisbox.core.Config;
import com.google.gson.Gson;

public class AlfrescoWebscripts {
	public static Changes getChangeLog(List<String> rootIds) throws Exception {
		String urlString = Config.getInstance().getRepositoryUrl()
				+ "/service/cmisbox/changes?id="
				+ rootIds.toString().replaceAll("\\[", "")
						.replaceAll("\\]", "");

		if (Config.getInstance().getChangeLogToken() != null) {
			urlString += "&token=" + Config.getInstance().getChangeLogToken();
		}
		String name = Config.getInstance().getRepositoryUsername();
		String password = Config.getInstance().getRepositoryPassword();

		String authString = name + ":" + password;
		byte[] authEncBytes = Base64.encode(authString.getBytes());
		String authStringEnc = new String(authEncBytes);

		URL url = new URL(urlString);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic "
				+ authStringEnc);
		InputStream is = urlConnection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);

		Gson gson = new Gson();
		Changes c = gson.fromJson(isr, Changes.class);

		return c;
	}

}
