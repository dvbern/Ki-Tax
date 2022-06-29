package ch.dvbern.ebegu.services.zahlungen;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaFooter;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ZahlungsfileGeneratorInfomaTest {

	@Test
	public void header() {
		final String actual = InfomaHeader.with(true, "Admin");
		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"));
		Assertions.assertNotNull(actual);
		Assertions.assertEquals("0|kiBon-DEV|" + today + "|" + now + "|Admin\n", actual);
	}

	@Test
	public void footer() {
		final String actual = InfomaFooter.with(4, BigDecimal.valueOf(1502.25));Assertions.assertNotNull(actual);
		Assertions.assertNotNull(actual);
		Assertions.assertEquals("9|4|1502,25\n", actual);
	}
}
