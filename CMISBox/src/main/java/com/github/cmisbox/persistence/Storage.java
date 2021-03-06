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

package com.github.cmisbox.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.github.cmisbox.core.Config;
import com.github.cmisbox.core.Main;
import com.github.cmisbox.core.Messages;
import com.github.cmisbox.local.Watcher;
import com.github.cmisbox.remote.CMISRepository;
import com.github.cmisbox.ui.UI;

public class Storage {
	public final static String indexFolderName = "indexes";

	public static final String FIELD_ROOT = "root";

	public static final String FIELD_VERSION = "version";

	public static final String FIELD_ID = "id";

	public static final String FIELD_TYPE = "type";

	public static final String FIELD_PATH = "path";

	public static final String FIELD_LOCAL_MODIFIED = "localModified";

	public static final String FIELD_REMOTE_MODIFIED = "remoteModified";

	public static final String TYPE_FOLDER = "folder";

	public static final String TYPE_FILE = "file";

	private static Storage instance = new Storage();

	public static Storage getInstance() {
		return Storage.instance;
	}

	private Log log;
	private Config config;

	private Directory directory;

	private IndexWriter writer;

	private IndexReader reader;

	private Storage() {
		this.log = LogFactory.getLog(this.getClass());
		this.config = Config.getInstance();
		File file = new File(this.config.getConfigHome(),
				Storage.indexFolderName);
		if (!file.exists()) {
			file.mkdirs();
			this.log.info(Messages.createdIndexFolder);
		}
		try {
			this.directory = FSDirectory.open(file);
			this.writer = new IndexWriter(this.directory,
					new IndexWriterConfig(Version.LUCENE_33,
							new StandardAnalyzer(Version.LUCENE_33)));

			this.reader = IndexReader.open(this.writer, false);

			List<String> roots = this.getRootPaths();

			for (String root : roots) {
				Watcher.getInstance().addWatch(root);
			}

		} catch (Exception e) {
			this.log.fatal(e);
			Main.exit(1);
		}

	}

	public void add(File f, CmisObject obj) throws Exception {
		if (obj.getBaseTypeId().value().equals(ObjectType.FOLDER_BASETYPE_ID)) {
			this.add(f, (Folder) obj, false);
		} else {
			this.add(f, (Document) obj);
		}
	}

	public void add(File file, Document document) throws Exception {
		FileOutputStream fos = new FileOutputStream(file);
		InputStream is = document.getContentStream().getStream();
		byte[] buffer = new byte[8192];
		int r = 0;
		while ((r = is.read(buffer)) != -1) {
			fos.write(buffer, 0, r);
		}
		fos.close();
		this.indexDocument(file, document);

	}

	public void add(File file, Folder folder, boolean root) throws Exception {
		file.mkdir();
		this.indexFolder(file, folder, root);
		for (CmisObject o : folder.getChildren()) {
			if (o.getBaseTypeId().value().equals(ObjectType.FOLDER_BASETYPE_ID)) {
				File nf = new File(file, o.getName());
				this.add(nf, (Folder) o, false);
			} else if (o.getBaseTypeId().value()
					.equals(ObjectType.DOCUMENT_BASETYPE_ID)) {
				File nf = new File(file, o.getName());
				this.add(nf, (Document) o);
			}
		}

	}

	public void close() {
		try {
			this.writer.close();
		} catch (Exception e) {
			this.log.error(e);
		}

	}

	public void commit() throws Exception {
		this.writer.commit();
	}

	public void delete(StoredItem item, boolean deleteChildren)
			throws Exception {
		this.writer.deleteDocuments(new Term(Storage.FIELD_PATH, item.getPath()
				+ (deleteChildren ? "*" : "")));
		this.commit();
	}

	public void deleteById(String id) throws Exception {
		StoredItem item = this.findById(id);
		if (item != null) {
			this.delete(item, true);
		}
	}

	public StoredItem findById(String id) throws Exception {
		id = id.split(";")[0];
		Query query = new TermQuery(new Term(Storage.FIELD_ID, id));
		TopDocs search = this.getSearcher().search(query, 1);
		if (search.totalHits == 0) {
			return null;
		}
		org.apache.lucene.document.Document d = this.reader
				.document(search.scoreDocs[0].doc);

		return new StoredItem(0, d.getFieldable(Storage.FIELD_ID),
				d.getFieldable(Storage.FIELD_TYPE),
				d.getFieldable(Storage.FIELD_PATH),
				d.getFieldable(Storage.FIELD_LOCAL_MODIFIED),
				d.getFieldable(Storage.FIELD_REMOTE_MODIFIED),
				d.getFieldable(Storage.FIELD_VERSION));
	}

	public List<StoredItem> findByPath(String path) throws Exception {
		Query query = new TermQuery(new Term(Storage.FIELD_PATH, path));
		TopDocs search = this.getSearcher().search(query, 99999);
		List<StoredItem> res = new ArrayList<StoredItem>(search.totalHits);
		for (int i = 0; i < search.totalHits; i++) {
			org.apache.lucene.document.Document d = this.reader
					.document(search.scoreDocs[i].doc);

			res.add(new StoredItem(i, d.getFieldable(Storage.FIELD_ID), d
					.getFieldable(Storage.FIELD_TYPE), d
					.getFieldable(Storage.FIELD_PATH), d
					.getFieldable(Storage.FIELD_LOCAL_MODIFIED), d
					.getFieldable(Storage.FIELD_REMOTE_MODIFIED), d
					.getFieldable(Storage.FIELD_VERSION)));
		}
		return res;
	}

	public Long getLastRemoteModification() {
		return null;
	}

	private String getNewPath(StoredItem storedItem, File file,
			StoredItem baseItem) {
		String newPathBase = file.getAbsolutePath().substring(
				Config.getInstance().getWatchParent().length());

		return (newPathBase + storedItem.getPath().substring(
				baseItem.getPath().length()));
	}

	public List<String> getRootIds() throws Exception {
		return this.getRootsField(Storage.FIELD_ID);
	}

	private List<String> getRootPaths() throws Exception {
		return this.getRootsField(Storage.FIELD_PATH);
	}

	public List<String> getRootsField(String field) throws Exception {
		Query query = new TermQuery(new Term(Storage.FIELD_ROOT, "" + true));
		TopDocs search = this.getSearcher().search(query, 99999);
		List<String> roots = new ArrayList<String>();
		for (ScoreDoc sd : search.scoreDocs) {
			org.apache.lucene.document.Document doc = this.reader
					.document(sd.doc);
			roots.add(doc.get(field));

		}
		return roots;
	}

	private IndexSearcher getSearcher() throws Exception {
		this.reader = this.reader.reopen();
		return new IndexSearcher(this.reader);
	}

	private void index(StoredItem si) throws Exception {
		org.apache.lucene.document.Document ldoc = new org.apache.lucene.document.Document();

		ldoc.add(new Field(Storage.FIELD_PATH, si.getPath(), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_TYPE, si.getType(), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_ID, si.getId(), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_VERSION, si.getVersion(), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_LOCAL_MODIFIED, DateTools
				.timeToString(si.getLocalModified(), Resolution.MILLISECOND),
				Store.YES, Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_REMOTE_MODIFIED, DateTools
				.timeToString(si.getRemoteModified(), Resolution.MILLISECOND),
				Store.YES, Index.NOT_ANALYZED));
		this.writer.addDocument(ldoc);
		this.log.debug(String.format("Indexed %s", ldoc));

	}

	private void indexDocument(File file, Document document)
			throws CorruptIndexException, IOException {
		org.apache.lucene.document.Document ldoc = new org.apache.lucene.document.Document();

		ldoc.add(new Field(Storage.FIELD_PATH, file.getAbsolutePath()
				.substring(this.config.getWatchParent().length()), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_TYPE, Storage.TYPE_FILE, Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_ID, document.getId().split(";")[0],
				Store.YES, Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_VERSION, document.getVersionLabel(),
				Store.YES, Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_LOCAL_MODIFIED, DateTools
				.timeToString(file.lastModified(), Resolution.MILLISECOND),
				Store.YES, Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_REMOTE_MODIFIED, DateTools
				.timeToString(document.getLastModificationDate()
						.getTimeInMillis(), Resolution.MILLISECOND), Store.YES,
				Index.NOT_ANALYZED));
		this.writer.addDocument(ldoc);
		this.log.debug(String.format("Indexed %s", ldoc));
	}

	private void indexFolder(File file, Folder folder, boolean root)
			throws CorruptIndexException, IOException {

		org.apache.lucene.document.Document ldoc = new org.apache.lucene.document.Document();

		ldoc.add(new Field(Storage.FIELD_PATH, file.getAbsolutePath()
				.substring(this.config.getWatchParent().length()), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_TYPE, Storage.TYPE_FOLDER, Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_ROOT, "" + root, Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_ID, folder.getId().split(";")[0],
				Store.YES, Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_LOCAL_MODIFIED, DateTools
				.timeToString(file.lastModified(), Resolution.MILLISECOND),
				Store.YES, Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_REMOTE_MODIFIED, DateTools
				.timeToString(folder.getLastModificationDate()
						.getTimeInMillis(), Resolution.MILLISECOND), Store.YES,
				Index.NOT_ANALYZED));

		this.writer.addDocument(ldoc);
		this.log.debug(String.format("Indexed %s", ldoc));

	}

	public void localUpdate(StoredItem item, File f, CmisObject obj)
			throws Exception {
		this.delete(item, false);
		if (f.isFile()) {
			this.indexDocument(f, (Document) obj);
		} else {
			for (StoredItem si : this.findByPath(item.getPath() + "*")) {
				this.delete(si, false);
				String path = this.getNewPath(si, f, item);
				si.setPath(path);
				this.index(si);
			}
		}
	}

	public void synchRemoteFolder(String id, String name) throws Exception {
		UI.getInstance().setStatus(UI.Status.SYNCH);
		Folder folder = CMISRepository.getInstance().getFolder(id);
		File destFolder = new File(this.config.getWatchParent(), name);
		if (destFolder.exists()) {
			this.log.error(Messages.synchAlreadyExisting);
			if (UI.getInstance().isAvailable()) {
				UI.getInstance().notify(Messages.synchAlreadyExisting);
			}
		} else {
			this.add(destFolder, folder, true);
			this.writer.commit();
			Watcher.getInstance().addWatch(
					destFolder.getAbsolutePath().substring(
							Config.getInstance().getWatchParent().length()));
			UI.getInstance().notify(
					Messages.folder + " " + destFolder.getName() + " "
							+ Messages.isSynchronized);
		}
		UI.getInstance().setStatus(UI.Status.OK);
	}
}
