/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service zum Speichern von Files auf dem File-System
 */
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
@Stateless
@Local(FileSaverService.class)
public class FileSaverServiceBean implements FileSaverService {

	private static final Logger LOG = LoggerFactory.getLogger(FileSaverServiceBean.class.getSimpleName());

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Override
	public void save(UploadFileInfo uploadFileInfo, String folderName) {
		Objects.requireNonNull(uploadFileInfo);
		Objects.requireNonNull(uploadFileInfo.getFilename());

		UUID uuid = UUID.randomUUID();

		String ending = getFileNameEnding(uploadFileInfo.getFilename());

		// Wir speichern der Name des Files nicht im FS. Kann sonst Probleme mit Umlauten geben
		final String absoluteFilePath = ebeguConfiguration.getDocumentFilePath() + '/' + folderName + '/' + uuid + '.' + ending;
		uploadFileInfo.setPath(absoluteFilePath);
		uploadFileInfo.setActualFilename(uuid + "." + ending);

		Path file = Paths.get(absoluteFilePath);
		try {
			if (!Files.exists(file.getParent())) {
				Files.createDirectories(file.getParent());
			}
			uploadFileInfo.setSize(Files.size(Files.write(file, uploadFileInfo.getBytes()))); //here we write to filesystem
			LOG.info("Save file in FileSystem: {}", absoluteFilePath);

		} catch (IOException e) {
			throw new EbeguRuntimeException("save", "Could not save file in filesystem {0}", e, absoluteFilePath);
		}
	}

	@Nonnull
	@Override
	public UploadFileInfo save(byte[] bytes, String fileName, String folderName) throws MimeTypeParseException {
		MimeType contentType = new MimeType("application/pdf");
		return save(bytes, fileName, folderName, contentType);
	}

	@Nonnull
	@Override
	public UploadFileInfo saveZipFile(
		@Nonnull byte[] bytes,
		@Nonnull String filename
	) throws MimeTypeParseException {

		String filenameWithEnding = filename + ".zip";

		final UploadFileInfo uploadFileInfo = new UploadFileInfo(filenameWithEnding, new MimeType("application/zip"));
		uploadFileInfo.setBytes(bytes);
		Objects.requireNonNull(uploadFileInfo);
		Objects.requireNonNull(uploadFileInfo.getFilename());

		final String absoluteFilePath = ebeguConfiguration.getDocumentFilePath() + "/auftraege/" + filenameWithEnding;
		uploadFileInfo.setPath(absoluteFilePath);
		uploadFileInfo.setActualFilename(filenameWithEnding);

		Path file = Paths.get(absoluteFilePath);
		try {
			if (!Files.exists(file.getParent())) {
				Files.createDirectories(file.getParent());
			}
			uploadFileInfo.setSize(Files.size(Files.write(file, uploadFileInfo.getBytes()))); //here we write to filesystem
			LOG.info("Save file in FileSystem: {}", absoluteFilePath);

		} catch (IOException e) {
			throw new EbeguRuntimeException("save", "Could not save file in filesystem {0}", e, absoluteFilePath);
		}
		return uploadFileInfo;
	}

	@Nonnull
	@Override
	public UploadFileInfo save(byte[] bytes, String fileName, String folderName, MimeType contentType) {
		final UploadFileInfo uploadFileInfo = new UploadFileInfo(fileName, contentType);
		uploadFileInfo.setBytes(bytes);
		save(uploadFileInfo, folderName);
		return uploadFileInfo;
	}

	@Override
	public boolean copy(FileMetadata fileToCopy, String folderName) {
		Objects.requireNonNull(fileToCopy);
		Objects.requireNonNull(folderName);

		Path oldfile = Paths.get(fileToCopy.getFilepfad());
		UUID uuid = UUID.randomUUID();
		String ending = getFileNameEnding(fileToCopy.getFilename());

		// Wir speichern der Name des Files nicht im FS. Kann sonst Probleme mit Umlauten geben
		final String absoluteFilePath = ebeguConfiguration.getDocumentFilePath() + '/' + folderName + '/' + uuid + '.' + ending;
		fileToCopy.setFilepfad(absoluteFilePath);

		Path newfile = Paths.get(absoluteFilePath);
		try {
			if (!Files.exists(newfile.getParent())) {
				Files.createDirectories(newfile.getParent());
				LOG.info("Save file in FileSystem: {}", absoluteFilePath);
			}
			Files.copy(oldfile, newfile);

		} catch (IOException e) {
			LOG.error("Can't save file in FileSystem: {}", fileToCopy.getFilename(), e);
			return false;
		}
		return true;
	}

	private String getFileNameEnding(String filename) {
		return FilenameUtils.getExtension(filename);
	}

	@Override
	public boolean remove(String dokumentPaths) {
		final Path path = Paths.get(dokumentPaths);
		try {
			if (Files.exists(path)) {
				Files.delete(path);
				LOG.info("Delete file in FileSystem: {}", dokumentPaths);
			}
		} catch (IOException e) {
			LOG.error("Can't remove file in FileSystem: {}", dokumentPaths, e);
			return false;
		}
		return true;
	}

	@Override
	public boolean removeAllFromSubfolder(@Nonnull String subfolder) {
		final String absoluteFilePath = ebeguConfiguration.getDocumentFilePath() + '/' + subfolder + '/';
		Path file = Paths.get(absoluteFilePath);
		try {
			if (Files.exists(file) && Files.isDirectory(file)) {
				FileUtils.cleanDirectory(file.toFile());
				Files.deleteIfExists(file);
				LOG.info("Deleting directory : {}", absoluteFilePath);
			}
			return true;
		} catch (IOException e) {
			LOG.error("Can't delete directory: {}", absoluteFilePath, e);
			return false;
		}
	}

	@Override
	public void deleteAllFilesInTempReportsFolder() {
		deleteAllFilesInTempFolder(Constants.TEMP_REPORT_FOLDERNAME);
	}

	@Override
	public void deleteAllFilesInTempNotverordnungFolder() {
		deleteAllFilesInTempFolder(Constants.TEMP_NOTVERORDNUNG);
	}

	private void deleteAllFilesInTempFolder(@Nonnull String folder) {
		final String absoluteFilePath = ebeguConfiguration.getDocumentFilePath() + '/' + folder;
		Path tempFolder = Paths.get(absoluteFilePath);
		try {
			if (Files.exists(tempFolder) && Files.isDirectory(tempFolder)) {
				Files.walk(tempFolder)
					.filter(Files::isRegularFile)
					.forEach(file -> deleteFileIfTokenExpired(file));
			}
		} catch (IOException e) {
			throw new EbeguRuntimeException("save", "Could not save file in filesystem {0}", e, absoluteFilePath);
		}
	}

	private void deleteFileIfTokenExpired(Path path) {
		LocalDateTime deleteBefore = LocalDateTime.now().minusMinutes(Constants.MAX_LONGER_TEMP_DOWNLOAD_AGE_MINUTES);
		LocalDateTime lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(path.toFile().lastModified()), ZoneId.systemDefault());
		if (lastModified.isBefore(deleteBefore)) {
			LOG.info("Deleting File {}, lastModified on {}",  path.getFileName(), lastModified);
			try {
				Files.delete(path);
			} catch (IOException e) {
				LOG.error("Can't delete file in FileSystem: {}", path.getFileName(), e);
			}
		}
	}
}
