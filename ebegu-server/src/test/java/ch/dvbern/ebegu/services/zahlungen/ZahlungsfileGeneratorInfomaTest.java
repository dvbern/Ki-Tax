package ch.dvbern.ebegu.services.zahlungen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaFooter;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaHeader;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaStammdatenFinanzbuchhaltung;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaStammdatenZahlung;
import ch.dvbern.ebegu.test.ZahlungsauftragBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ZahlungsfileGeneratorInfomaTest {

	@Test
	public void header() {
		final String actual = InfomaHeader.with(true, "Admin");
		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"));
		Assertions.assertNotNull(actual);
		final String expected = "0|kiBon-DEV|" + today + "|" + now + "|Admin\n";
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void footer() {
		final String actual = InfomaFooter.with(4, BigDecimal.valueOf(1502.25));Assertions.assertNotNull(actual);
		Assertions.assertNotNull(actual);
		final String expected = "9|4|1502.25\n";
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void stammdaten() {
		Zahlungsauftrag auftrag = ZahlungsauftragBuilder.create(builder -> builder
			.withZahlungslauftyp(ZahlungslaufTyp.GEMEINDE_INSTITUTION)
			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 31))
			.withDatumFaellig(LocalDate.of(2022, Month.AUGUST, 31))
			.withBeschrieb("Zahlungslauf August 2022")
			.withZahlung(BigDecimal.valueOf(423.25), "Kita Brünnen", "419081")
		);
		Zahlung zahlung = auftrag.getZahlungen().get(0);

		final String actualZahlung = InfomaStammdatenZahlung.with(zahlung, 200001);
		final String externeId = "21.000001.002.1.1";
		final String expectedZahlung = "1|2|BGR200001|" + externeId + "|31.08.2022||2|419081||||Kita Bruennen - Zahlungslauf August 2022|1|215|||||||||||||||||-423.25||31.08.2022|||||||||||||||||||||||||||||||010|||||BG 2022, 8, Kita Bruennen|||\n";
		Assertions.assertEquals(expectedZahlung, actualZahlung);

		final String actualFinanzbuchhaltung = InfomaStammdatenFinanzbuchhaltung.with(zahlung, 200001);
		final String expectedFinanzbuchhaltung = "1|2|BGR200001|" + externeId + "|31.08.2022||0|3636.022||||Kita Bruennen - Zahlungslauf August 2022|1|215|||2158303||||||||||||||423.25|||||||||||||||||||||||||||||||||RB IBAN|||||BG 2022, 8, Kita Bruennen|||\n";
		Assertions.assertEquals(expectedFinanzbuchhaltung, actualFinanzbuchhaltung);
	}
}
