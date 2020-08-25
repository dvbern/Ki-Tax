/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * Creates a zip file. Use {@link ZipCreator#append} to append documents and
 * {@link ZipCreator#create} to get the created zip.
 */
public class ZipCreator {

	private static final int ZIP_BUFFER_SIZE = 1024 * 8;

	private static final String PATH_SEPARATOR = "/";

	@Nonnull
	private final ZipOutputStream zipOutputStream;

	@Nonnull
	private final ByteArrayOutputStream outputStream;

	public ZipCreator() {
		outputStream = new ByteArrayOutputStream();
		zipOutputStream = new ZipOutputStream(outputStream);
	}

	/**
	 * Append a file to this zip.
	 *
	 * @param binaryStream binary stream of the file to be appended
	 * @param path full path inside the zip to the file - can contain multiple parts that will be joined
	 * together
	 */
	public void append(@Nonnull InputStream binaryStream, @Nonnull String... path) throws IOException {
		String filename = joinPath(path);
		ZipEntry entry = new ZipEntry(filename);

		zipOutputStream.putNextEntry(entry);

		int len;
		byte[] buffer = new byte[ZIP_BUFFER_SIZE];

		while ((len = binaryStream.read(buffer)) > 0) {
			zipOutputStream.write(buffer, 0, len);
		}

		zipOutputStream.closeEntry();
		binaryStream.close();
	}

	/**
	 * Creates the zip file with the previously appended files.
	 */
	@Nonnull
	public byte[] create() throws IOException {
		zipOutputStream.close();
		return outputStream.toByteArray();
	}

	@Nonnull
	private static String joinPath(@Nonnull String... parts) {
		return String.join(PATH_SEPARATOR, parts);
	}
}
