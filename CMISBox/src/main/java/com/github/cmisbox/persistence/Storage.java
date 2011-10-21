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
	private static final String FIELD_VERSION = "version";

	public static final String FIELD_ID = "id";

	public static final String FIELD_LAST_MODIFIED = "lastModified";

	public static final String TYPE_FOLDER = "folder";

	public static final String FIELD_TYPE = "type";

	public static final String FIELD_PATH = "path";

	private static Storage instance = new Storage();

	public final static String indexFolderName = "indexes";

	private static final String TYPE_FILE = "file";

	private static final String FIELD_ROOT = "root";

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

			Query query = new TermQuery(new Term(Storage.FIELD_ROOT, "" + true));
			TopDocs search = this.getSearcher().search(query, 99999);
			for (ScoreDoc sd : search.scoreDocs) {
				org.apache.lucene.document.Document doc = this.reader
						.document(sd.doc);
				Watcher.getInstance().addWatch(doc.get(Storage.FIELD_PATH));

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

	public void delete(StoredItem item) throws Exception {
		this.writer.deleteDocuments(new Term(Storage.FIELD_ID, item.getId()));
		this.commit();
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
					.getFieldable(Storage.FIELD_LAST_MODIFIED), d
					.getFieldable(Storage.FIELD_VERSION)));
		}
		return res;
	}

	private IndexSearcher getSearcher() throws Exception {
		this.reader = this.reader.reopen();
		return new IndexSearcher(this.reader);
	}

	private void indexDocument(File file, Document document)
			throws CorruptIndexException, IOException {
		org.apache.lucene.document.Document ldoc = new org.apache.lucene.document.Document();

		ldoc.add(new Field(Storage.FIELD_PATH, file.getAbsolutePath()
				.substring(this.config.getWatchParent().length()), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_TYPE, Storage.TYPE_FILE, Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_ID, document.getId(), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_VERSION, document.getVersionLabel(),
				Store.YES, Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_LAST_MODIFIED, DateTools.timeToString(
				file.lastModified(), Resolution.MILLISECOND), Store.YES,
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
		ldoc.add(new Field(Storage.FIELD_ID, folder.getId(), Store.YES,
				Index.NOT_ANALYZED));
		ldoc.add(new Field(Storage.FIELD_LAST_MODIFIED, DateTools.timeToString(
				file.lastModified(), Resolution.MILLISECOND), Store.YES,
				Index.NOT_ANALYZED));
		this.writer.addDocument(ldoc);
		this.log.debug(String.format("Indexed %s", ldoc));

	}

	public void synchRemoteFolder(String id) throws Exception {
		UI.getInstance().setStatus(UI.Status.SYNCH);
		Folder folder = CMISRepository.getInstance().getFolder(id);
		File destFolder = new File(this.config.getWatchParent(),
				folder.getName());
		if (destFolder.exists()) {
			this.log.error(Messages.synchAlreadyExisting);
			if (UI.getInstance().isAvailable()) {
				UI.getInstance().notify(Messages.synchAlreadyExisting);
			}
		} else {
			this.add(destFolder, folder, true);
			this.writer.commit();
			Watcher.getInstance().addWatch(destFolder.getAbsolutePath());
			UI.getInstance().notify(
					Messages.folder + " " + destFolder.getName() + " "
							+ Messages.synchAlreadyExisting);
		}
		UI.getInstance().setStatus(UI.Status.OK);
	}

}
