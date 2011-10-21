package com.github.cmisbox.remote;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.client.util.FileUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.cmisbox.core.Config;
import com.github.cmisbox.core.Main;
import com.github.cmisbox.core.Messages;
import com.github.cmisbox.ui.UI;
import com.github.cmisbox.ui.UI.Status;

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
							UI.getInstance().setStatus(UI.Status.OK);
						} catch (Exception e) {
							CMISRepository.this.log.error(e);
							UI.getInstance().setStatus(Status.KO);
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

	public CmisObject addChild(String id, File f) throws Exception {
		if (f.isDirectory()) {
			return FileUtils.createFolder(id, f.getName(), null, this.session);
		} else {
			return FileUtils.createDocumentFromFile(id, f, null,
					VersioningState.NONE, this.session);
		}

	}

	public void delete(String id) {
		FileUtils.delete(id, this.session);
	}

	public TreeMap<String, String> getChildrenFolders(String id) {
		TreeMap<String, String> res = new TreeMap<String, String>();
		Iterator<QueryResult> i = this.session
				.query("select * from cmis:folder where in_folder('" + id
						+ "')", true).iterator();
		while (i.hasNext()) {
			QueryResult qr = i.next();
			res.put(qr.getPropertyById(PropertyIds.NAME).getFirstValue()
					.toString(), qr.getPropertyById(PropertyIds.OBJECT_ID)
					.getFirstValue().toString());
		}
		return res;
	}

	public Folder getFolder(String id) {
		try {
			return (Folder) this.session.getObject(id);
		} catch (Exception e) {
			this.log.error(e);
			return null;
		}
	}

	public TreeMap<String, String> getRoots() {
		return this.getChildrenFolders(this.session.getRootFolder().getId());
	}

	public CmisObject rename(String id, File f) {
		CmisObject obj = this.session.getObject(id);
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (Property<?> p : obj.getProperties()) {
			map.put(p.getId(), p.getValue());
		}
		obj.updateProperties(map, true);
		return obj;
	}

	public Document update(String id, File f) throws Exception {
		Document doc = (Document) this.session.getObject(id);
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (Property<?> p : doc.getProperties()) {
			map.put(p.getId(), p.getValue());
		}

		map.put(PropertyIds.NAME, f.getName());

		ContentStreamImpl contentStreamImpl = new ContentStreamImpl(
				f.getName(), new BigInteger("" + f.length()),
				MimeTypes.getMIMEType(f), new FileInputStream(f));
		doc.checkIn(true, map, contentStreamImpl, "By CMISBox");

		return null;

	}

}
