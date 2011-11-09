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
import com.github.cmisbox.persistence.Storage;
import com.github.cmisbox.persistence.StoredItem;
import com.github.cmisbox.ui.UI;
import com.github.cmisbox.ui.UI.Status;

public class CMISRepository {

	public static final String CONFLICT = "CONFLICT";

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
		parameter.put(SessionParameter.ATOMPUB_URL, url + "/s/cmis");
		parameter.put(SessionParameter.BINDING_TYPE,
				BindingType.ATOMPUB.value());

		// parameter.put(SessionParameter.BINDING_TYPE,
		// BindingType.WEBSERVICES.value());
		// parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url
		// + "/cmis/ACLService?wsdl");
		// parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url
		// + "/cmis/DiscoveryService?wsdl");
		// parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url
		// + "/cmis/MultiFilingService?wsdl");
		// parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url
		// + "/cmis/NavigationService?wsdl");
		// parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url
		// + "/cmis/ObjectService?wsdl");
		// parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url
		// + "/cmis/PolicyService?wsdl");
		// parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url
		// + "/cmis/RelationshipService?wsdl");
		// parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url
		// + "/cmis/RepositoryService?wsdl");
		// parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url
		// + "/cmis/VersioningService?wsdl");

		// create session
		org.apache.chemistry.opencmis.client.api.Repository r = f
				.getRepositories(parameter).get(0);

		parameter.put(SessionParameter.REPOSITORY_ID, r.getId());

		CMISRepository.instance.session = f.createSession(parameter);

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
					if (CMISRepository.this.session != null) {
						try {
							Long lrm = Storage.getInstance()
									.getLastRemoteModification();

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

			private void synchAllWatches(Long lrm) {
				// TODO Auto-generated method stub

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

	public void download(Document doc, File file) throws Exception {
		FileUtils.download(doc, file.getAbsolutePath());
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

	public Document getDocument(String id) {
		try {
			return (Document) this.session.getObject(id);
		} catch (Exception e) {
			this.log.error(e);
			return null;
		}
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
		String newName = f.getName();
		return this.rename(id, newName);
	}

	private CmisObject rename(String id, String newName) {
		CmisObject obj = this.session.getObject(id);
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (Property<?> p : obj.getProperties()) {
			map.put(p.getId(), p.getValue());
		}
		map.put(PropertyIds.NAME, newName);
		obj.updateProperties(map, true);
		return obj;
	}

	public Document update(StoredItem item, File f) throws Exception {
		Document doc = (Document) this.session.getObject(item.getId());
		doc.refresh();

		HashMap<String, Object> map = new HashMap<String, Object>();
		for (Property<?> p : doc.getProperties()) {
			map.put(p.getId(), p.getValue());
		}

		String versionComment = "By CMISBox";

		if (!item.getVersion().equals(doc.getVersionLabel())) {
			versionComment += " " + CMISRepository.CONFLICT;
			String conflictName = f.getAbsolutePath();
			int i = conflictName.lastIndexOf(".");
			conflictName = conflictName.substring(0, i) + "_"
					+ doc.getVersionLabel() + "_CONFLICT_"
					+ doc.getLastModifiedBy() + conflictName.substring(i);

			FileUtils.download(doc, conflictName);
			if (UI.getInstance().isAvailable()) {
				UI.getInstance().notify(
						Messages.conflict + " : " + conflictName);
			}
		}

		map.put(PropertyIds.NAME, f.getName());

		ContentStreamImpl contentStreamImpl = new ContentStreamImpl(
				f.getName(), new BigInteger("" + f.length()),
				MimeTypes.getMIMEType(f), new FileInputStream(f));

		doc.checkIn(false, map, contentStreamImpl, versionComment);

		doc.refresh();

		return doc;

	}

}
