package com.github.cmisbox.remote;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.cmisbox.core.Config;
import com.github.cmisbox.core.Main;
import com.github.cmisbox.ui.UI;

public class CMISRepository {

	private Thread connector;

	private Session session;

	private Log log;

	private static CMISRepository instance = new CMISRepository();

	public static CMISRepository getInstance() {
		return CMISRepository.instance;
	}

	private CMISRepository() {
		this.log = LogFactory.getLog(this.getClass());

		final String url = Config.getInstance().getRepositoryUrl();
		final String username = Config.getInstance().getRepositoryUsername();
		final String password = Config.getInstance().getRepositoryPassword();

		if ((url == null) || (username == null) || (password == null)) {
			UI ui = UI.getInstance();
			if (ui.isAvailable()) {
				ui.openRemoteLoginDialog();
			} else {
				this.log.error("Provide credentials in cmisbox.properties");
				Main.exit(1);
			}
		}

		this.connector = new Thread(new Runnable() {

			public void run() {
				while (true) {
					if (CMISRepository.this.session == null) {
						org.apache.chemistry.opencmis.client.api.SessionFactory f = SessionFactoryImpl
								.newInstance();
						Map<String, String> parameter = new HashMap<String, String>();

						// user credentials
						parameter.put(SessionParameter.USER, username);
						parameter.put(SessionParameter.PASSWORD, password);

						// connection settings
						parameter.put(SessionParameter.ATOMPUB_URL,
								url.concat("/s/cmis"));
						parameter.put(SessionParameter.BINDING_TYPE,
								BindingType.ATOMPUB.value());

						// session locale
						// parameter.put(SessionParameter.LOCALE_ISO3166_COUNTRY,
						// "");
						// parameter.put(SessionParameter.LOCALE_ISO639_LANGUAGE,
						// "it");
						// parameter.put(SessionParameter.LOCALE_VARIANT, "");

						// create session

						org.apache.chemistry.opencmis.client.api.Repository r = f
								.getRepositories(parameter).get(0);

						parameter.put(SessionParameter.REPOSITORY_ID, r.getId());

						com.github.cmisbox.remote.CMISRepository.this.session = f
								.createSession(parameter);

					}
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});

		this.connector.start();

	}
}
