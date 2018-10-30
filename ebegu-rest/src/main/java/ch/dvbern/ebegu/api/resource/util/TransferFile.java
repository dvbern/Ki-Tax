/*
 * AGPL File-Header
 *
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource.util;

import java.util.Arrays;

public class TransferFile {

	public static final byte[] EMPTY_ARRAY = new byte[0];
	private String filename;
	private long filesize;
	private String filetype;
	private byte[] content;

	public TransferFile(String filename, String filetype, byte[] content) {
		this.filename = filename;
		this.filesize = content.length;
		this.filetype = filetype;
		this.setContent(content);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public String getFiletype() {
		return filetype;
	}

	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

	public byte[] getContent() {
		if (content == null) {
			return EMPTY_ARRAY;
		}
		return Arrays.copyOf(content, content.length);
	}

	public void setContent(byte[] content) {
		if (content == null) {
			this.content = EMPTY_ARRAY;
		} else {
			this.content = Arrays.copyOf(content, content.length);
		}
	}

}
