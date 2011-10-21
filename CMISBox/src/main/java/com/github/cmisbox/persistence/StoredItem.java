package com.github.cmisbox.persistence;

import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Fieldable;

public class StoredItem {
	private Integer docNumber;
	private String id;
	private String type;
	private String path;
	private Long lastModified;
	private String version;

	public StoredItem() {
	}

	public StoredItem(int docNumber, Fieldable id, Fieldable type,
			Fieldable path, Fieldable lastModified, Fieldable version)
			throws Exception {
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
		if (lastModified != null) {
			this.lastModified = DateTools.stringToTime(lastModified
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

	public Long getLastModified() {
		return this.lastModified;
	}

	public String getPath() {
		return this.path;
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

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	public void setPath(String path) {
		this.path = path;
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
				this.id, this.type, this.path, new Date(this.lastModified),
				this.version);
	}
}
