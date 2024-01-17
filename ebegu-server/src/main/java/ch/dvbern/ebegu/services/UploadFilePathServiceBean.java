package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.nio.file.Path;

@Stateless
@Local(UploadFilePathService.class)
public class UploadFilePathServiceBean implements UploadFilePathService {

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Override
	public Path getValidatedFilePathWithDirectoryPrefix(@Nonnull Path path) {
		Path ebeguPath = Path.of(ebeguConfiguration.getDocumentFilePath()).resolve(path);
		return getValidatedFilePath(ebeguPath);
	}

	@Override
	public Path getValidatedFilePath(@Nonnull Path path) {
		Path normalizedPath = path.normalize();
		if (!normalizedPath.startsWith(ebeguConfiguration.getDocumentFilePath())) {
			throw new EbeguRuntimeException("validate file", "illegal document path");
		}
		return normalizedPath;
	}
}
