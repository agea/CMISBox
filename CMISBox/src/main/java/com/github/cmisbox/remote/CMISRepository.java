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
import com.github.cmisbox.core.Messages;
import com.github.cmisbox.ui.UI;

public class CMISRepository {

	private Thread connector;

	private Session session;

	private Log log;

	private static CMISRepository instance = new CMISRepository();

	public static void doLogin() throws Exception {

		String url = Config.getInstance().getRepositoryUrl();
		String username = Config.getInstance().getRepositoryUsername();
		String password = Config.getInstance().getRepositoryPassword();

		if ((url == null) || (username == null) || (password == null)) {
			UI ui = UI.getInstance();
			if (ui.isAvailable()) {
				ui.openRemoteLoginDialog();
			} else {
				LogFactory.getLog(CMISRepository.class).error(
						Messages.provideCredentialsInProps);
				Main.exit(1);
			}
		}

		org.apache.chemistry.opencmis.client.api.SessionFactory f = SessionFactoryImpl
				.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, username);
		parameter.put(SessionParameter.PASSWORD, password);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE,
				BindingType.ATOMPUB.value());

		// create session
		org.apache.chemistry.opencmis.client.api.Repository r = f
				.getRepositories(parameter).get(0);

		parameter.put(SessionParameter.REPOSITORY_ID, r.getId());

		com.github.cmisbox.remote.CMISRepository.instance.session = f
				.createSession(parameter);

	}

	public static CMISRepository getInstance() {
		return CMISRepository.instance;
	}

	private CMISRepository() {
		this.log = LogFactory.getLog(this.getClass());

		this.connector = new Thread(new Runnable() {

			public void run() {
				while (true) {
					if (CMISRepository.this.session == null) {
						try {
							CMISRepository.doLogin();
						} catch (Exception e) {
							CMISRepository.this.log.error(e);
						}
					}
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {

					}

				}
			}

		});

		this.connector.start();

	}
}
