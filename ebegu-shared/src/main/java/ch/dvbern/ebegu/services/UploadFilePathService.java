package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public interface UploadFilePathService {

	Path getValidatedFilePathWithDirectoryPrefix(@Nonnull String path);

	Path getValidatedFilePath(@Nonnull Path path);
}
