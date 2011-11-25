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
