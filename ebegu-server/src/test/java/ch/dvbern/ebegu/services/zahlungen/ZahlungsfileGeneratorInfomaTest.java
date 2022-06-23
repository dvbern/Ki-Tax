package ch.dvbern.ebegu.services.zahlungen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ZahlungsfileGeneratorInfomaTest {

	@Test
	public void header() {
		InfomaHeader header = new InfomaHeader(true, "Admin");
		final String actual = header.toString();
		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"));
		Assertions.assertNotNull(actual);
		Assertions.assertEquals("0|kiBon-DEV|" + today + "|" + now + "|Admin", actual);
	}
}
