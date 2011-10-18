package com.github.cmisbox.persistence;

public class StoredItem {
	private Integer docNumber;
	private String id;
	private String type;
	private String path;
	private Long lastModified;
	private String version;

	public StoredItem() {
	}

	public StoredItem(Integer docNumber, String id, String type, String path,
			Long lastModified, String version) {
		super();
		this.docNumber = docNumber;
		this.id = id;
		this.type = type;
		this.path = path;
		this.lastModified = lastModified;
		this.version = version;
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
}
