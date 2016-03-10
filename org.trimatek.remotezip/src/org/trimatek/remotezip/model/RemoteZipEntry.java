package org.trimatek.remotezip.model;

import java.util.zip.ZipEntry;

public class RemoteZipEntry extends ZipEntry {

	private int zipFileIndex;
	private int offset;

	public RemoteZipEntry(String name) {
		super(name);
	}

	public RemoteZipEntry(ZipEntry e) {
		super(e);
	}

	public int getZipFileIndex() {
		return zipFileIndex;
	}

	public void setZipFileIndex(int zipFileIndex) {
		this.zipFileIndex = zipFileIndex;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

}
