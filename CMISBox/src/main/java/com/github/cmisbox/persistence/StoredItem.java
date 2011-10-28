package com.github.cmisbox.persistence;

import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Fieldable;

public class StoredItem {
	private Integer docNumber;
	private String id;
	private String type;
	private String path;
	private Long localModified;
	private Long remoteModified;
	private String version;

	public StoredItem() {
	}

	public StoredItem(int docNumber, Fieldable id, Fieldable type,
			Fieldable path, Fieldable localModified, Fieldable remoteModified,
			Fieldable version) throws Exception {
		this.docNumber = docNumber;
		if (id != null) {
			this.id = id.stringValue();
		}
		if (type != null) {
			this.type = type.stringValue();
		}
		if (path != null) {
			this.path = path.stringValue();
		}
		if (localModified != null) {
			this.localModified = DateTools.stringToTime(localModified
					.stringValue());
		}
		if (remoteModified != null) {
			this.remoteModified = DateTools.stringToTime(remoteModified
					.stringValue());
		}
		if (version != null) {
			this.setVersion(version.stringValue());
		}
	}

	public Integer getDocNumber() {
		return this.docNumber;
	}

	public String getId() {
		return this.id;
	}

	public Long getLocalModified() {
		return this.localModified;
	}

	public String getPath() {
		return this.path;
	}

	public Long getRemoteModified() {
		return this.remoteModified;
	}

	public String getType() {
		return this.type;
	}

	public String getVersion() {
		return this.version;
	}

	public void setDocNumber(Integer docNumber) {
		this.docNumber = docNumber;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLocalModified(Long lastModified) {
		this.localModified = lastModified;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setRemoteModified(Long remoteModified) {
		this.remoteModified = remoteModified;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return String.format("StoredItem[%d,%s,$s,%s,%s,%s]", this.docNumber,
				this.id, this.type, this.path, new Date(this.localModified),
				this.version);
	}
}
