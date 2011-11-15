package com.github.cmisbox.core;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.regex.Pattern;

import org.apache.chemistry.opencmis.client.api.ChangeEvent;
import org.apache.chemistry.opencmis.client.api.ChangeEvents;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.github.cmisbox.persistence.Storage;
import com.github.cmisbox.persistence.StoredItem;
import com.github.cmisbox.remote.CMISRepository;
import com.github.cmisbox.ui.UI;
import com.github.cmisbox.ui.UI.Status;

public class Queue implements Runnable {

	private static Queue instance = new Queue();

	public static Queue getInstance() {
		return Queue.instance;
	}

	private boolean active = true;

	// by default do not synch files starting with a dot
	private Pattern filter = Pattern.compile("^\\..*");

	private Thread thread;

	private DelayQueue<LocalEvent> delayQueue = new DelayQueue<LocalEvent>();

	private Log log;

	private Queue() {
		this.thread = new Thread(this, "Queue");
		this.thread.start();
		this.log = LogFactory.getLog(this.getClass());
	}

	public synchronized void add(LocalEvent localEvent) {
		this.log.debug("Asked to queue" + localEvent);
		if (!this.active) {
			return;
		}

		if ((localEvent.getName() != null)
				&& this.filter.pattern().matches(localEvent.getName())) {
			this.log.debug("Filtered " + localEvent);
			return;
		}

		if (this.delayQueue.contains(localEvent) || localEvent.isRename()) {
			Iterator<LocalEvent> i = this.delayQueue.iterator();
			while (i.hasNext()) {
				LocalEvent queuedEvent = i.next();
				if (queuedEvent.equals(localEvent)) {
					localEvent.merge(queuedEvent);
					i.remove();
					this.log.debug("" + "Merged " + queuedEvent);

				} else if (Config.getInstance().isMacOSX()
						&& localEvent.isRename() && queuedEvent.isDelete()) {
					if (localEvent.isParent(queuedEvent)) {
						i.remove();
					}
				}
			}
		}

		if (!(localEvent.isCreate() && localEvent.isDelete())) {
			this.delayQueue.put(localEvent);
			this.log.debug("Queued " + localEvent);
		}
	}

	public Pattern getFilter() {
		return this.filter;
	}

	private StoredItem getSingleItem(String path) throws Exception {
		List<StoredItem> itemList = Storage.getInstance().findByPath(path);
		if (itemList.size() == 1) {
			return itemList.get(0);
		} else {
			throw new Exception(String.format(
					"Expected one result in index: %s -> %s", path, itemList));
		}
	}

	public void manageEvent(LocalEvent event) {
		Log log = LogFactory.getLog(this.getClass());
		log.debug("managing: " + event);

		// any platform
		// - a folder can be renamed before containing files are managed: on
		// folder rename all children must be updated while still in queue;

		// linux
		// - if a file or folder is moved out of a watched folder it is reported
		// as a rename to null (check if it's still there)

		// mac osx
		// - recursive folder operations (e.g. unzip an archive or move a folder
		// inside a watched folder) are not reported, only root folder is
		// reported as create
		// - folder rename causes children to be notified as deleted (with old
		// path)

		try {
			if (event.isSynch()) {
				this.synchAllWatches();
				return;
			}

			File f = new File(event.getFullFilename());
			if (event.isCreate()) {
				String parent = f.getParent().substring(
						Config.getInstance().getWatchParent().length());

				CmisObject obj = CMISRepository.getInstance().addChild(
						this.getSingleItem(parent).getId(), f);
				Storage.getInstance().add(f, obj);
			} else if (event.isDelete()) {
				StoredItem item = this.getSingleItem(event.getLocalPath());
				if (f.exists()) {
					throw new Exception(String.format(
							"File %s reported to be deleted but stil exists",
							f.getAbsolutePath()));
				}
				CMISRepository.getInstance().delete(item.getId());
				Storage.getInstance().delete(item, true);
			} else if (event.isModify()) {
				if (f.isFile()) {
					StoredItem item = this.getSingleItem(event.getLocalPath());

					if (item.getLocalModified().longValue() < f.lastModified()) {

						Document doc = CMISRepository.getInstance().update(
								item, f);

						Storage.getInstance().localUpdate(item, f, doc);
					} else {
						log.debug("file" + f + " modified in the past");
					}

				}
			} else if (event.isRename()) {
				StoredItem item = this.getSingleItem(event.getLocalPath());
				CmisObject obj = CMISRepository.getInstance().rename(
						item.getId(), f);
				Storage.getInstance().localUpdate(item, f, obj);
			}

		} catch (Exception e) {
			log.error(e);
			if (UI.getInstance().isAvailable()) {
				UI.getInstance().notify(e.toString());
				UI.getInstance().setStatus(Status.KO);
			}
		}
	}

	public void run() {
		while (this.active) {
			try {
				this.manageEvent(this.delayQueue.take());
				if (this.delayQueue.isEmpty()) {

				}
			} catch (InterruptedException e) {
				LogFactory.getLog(this.getClass()).info(this, e);
			}
		}

	}

	public void setFilter(Pattern filter) {
		this.filter = filter;
	}

	public void stop() {
		this.active = false;
		this.delayQueue.clear();
		this.thread.interrupt();
	}

	private void synchAllWatches() throws Exception {
		UI ui = UI.getInstance();
		if (ui.isAvailable()) {
			ui.setStatus(Status.SYNCH);
		}
		List<String[]> updates = new ArrayList<String[]>();

		ChangeEvents changeEvents = CMISRepository.getInstance()
				.getContentChanges();
		for (ChangeEvent ce : changeEvents.getChangeEvents()) {
			System.out.println(ce);
			if (ce.getChangeType().equals(ChangeType.CREATED)) {
				//
			}
		}
		changeEvents.getLatestChangeLogToken();
		CMISRepository.getInstance().updateChangeLogToken();

		if (ui.isAvailable()) {
			ui.setStatus(Status.OK);
			if (updates.size() == 1) {
				ui.notify(updates.get(0)[0] + " " + Messages.updatedBy + " "
						+ updates.get(0)[1]);
			} else if (updates.size() > 1) {
				ui.notify(Messages.updated + " " + updates.size()
						+ Messages.files);
			}

		}
	}

	private void updateLocalChanges(List<String[]> updates,
			ItemIterable<QueryResult> iterable) throws Exception {
		do {
			for (QueryResult queryResult : iterable) {
				String objectId = queryResult
						.getPropertyValueById(PropertyIds.OBJECT_ID);
				String name = queryResult
						.getPropertyValueById(PropertyIds.NAME);
				GregorianCalendar lastModificationDate = queryResult
						.getPropertyValueById(PropertyIds.LAST_MODIFICATION_DATE);

				StoredItem item = Storage.getInstance().findById(objectId);
				if (item.getRemoteModified().longValue() >= lastModificationDate
						.getTimeInMillis()) {
					break;
				} else {
					File oldFile = new File(Config.getInstance()
							.getWatchParent(), item.getPath());
					if (item.getType().equals(Storage.TYPE_FOLDER)) {
						oldFile.renameTo(new File(oldFile.getParent(), name));
					} else {
						oldFile.delete();
						Document doc = CMISRepository.getInstance()
								.getDocument(objectId);
						File file = new File(oldFile.getParent(), name);
						CMISRepository.getInstance().download(doc, file);
						Storage.getInstance().delete(item, false);
						Storage.getInstance().add(file, doc);
						updates.add(new String[] {
								file.getAbsolutePath().substring(
										Config.getInstance().getWatchParent()
												.length()),
								doc.getLastModifiedBy() });
						Logger.getLogger(this.getClass()).debug(
								"Updated remote changes: "
										+ file.getAbsolutePath());
					}

				}
			}
		} while (iterable.getHasMoreItems());
	}
}
