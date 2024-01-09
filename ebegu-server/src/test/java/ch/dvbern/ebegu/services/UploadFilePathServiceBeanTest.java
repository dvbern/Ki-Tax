package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Path;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(EasyMockRunner.class)
public class UploadFilePathServiceBeanTest {

	@TestSubject
	private final UploadFilePathServiceBean filePathValidator = new UploadFilePathServiceBean();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private EbeguConfiguration ebeguConfiguration;

	private static final String FILE_PATH_CONFIG = "\\kibon\\data";

	@Before
	public void init() {
		expect(ebeguConfiguration.getDocumentFilePath()).andReturn(FILE_PATH_CONFIG).anyTimes();
		replay(ebeguConfiguration);
	}

	@Test
	public void ebeguFilePath() {
		Path filePath = Path.of(FILE_PATH_CONFIG, "test");
		Path validatedFilePath = filePathValidator.getValidatedFilePath(filePath);
		assertThat(validatedFilePath.toString(), is(FILE_PATH_CONFIG + "\\test"));
	}

	@Test(expected = EbeguRuntimeException.class)
	public void noEbeguFilePath() {
		Path filePath = Path.of("hello", "test");
		Path validatedFilePath = filePathValidator.getValidatedFilePath(filePath);
	}

	@Test
	public void validFilePath() {
		Path validatedFilePath = filePathValidator.getValidatedFilePathWithDirectoryPrefix("test");
		assertThat(validatedFilePath.toString(), is(FILE_PATH_CONFIG + "\\test"));
	}

	@Test
	public void validUriFilePath() {
		Path validatedFilePath = filePathValidator.getValidatedFilePathWithDirectoryPrefix("\\test");
		assertThat(validatedFilePath.toString(), is(FILE_PATH_CONFIG + "\\test"));
	}

	@Test
	public void validFilePathWithEnding() {
		Path validatedFilePath = filePathValidator.getValidatedFilePathWithDirectoryPrefix("test/file.txt");
		assertThat(validatedFilePath.toString(), is(FILE_PATH_CONFIG + "\\test\\file.txt"));
	}

	@Test(expected = EbeguRuntimeException.class)
	public void filePathWithDoubleDotPathStep() {
		Path validatedFilePath = filePathValidator.getValidatedFilePathWithDirectoryPrefix("\\..\\test");
	}

}
