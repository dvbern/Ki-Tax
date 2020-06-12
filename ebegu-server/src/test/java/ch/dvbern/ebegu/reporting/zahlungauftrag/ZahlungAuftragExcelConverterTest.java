package ch.dvbern.ebegu.reporting.zahlungauftrag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.enums.ZahlungspositionStatus;
import ch.dvbern.ebegu.types.DateRange;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class ZahlungAuftragExcelConverterTest {

	@Test
	public void filterTest() {
		String sameBG = "19.000101.001.1.2";
		LocalDate sameAb = LocalDate.of(2020, 8, 1);
		LocalDate sameBis = LocalDate.of(2020, 8, 31);
		BigDecimal sameBgPensum = BigDecimal.valueOf(0.4);
		BigDecimal sameBetrag = BigDecimal.valueOf(1451.3);
		BigDecimal sameBetragInverted = BigDecimal.valueOf(-1451.3);

		List<Zahlungsposition> zahlungspositionen = new LinkedList<>();
		zahlungspositionen.add(createZahlungsposition(sameBG, sameAb, sameBis, sameBgPensum, sameBetrag));
		zahlungspositionen.add(createZahlungsposition(sameBG, sameAb, sameBis, sameBgPensum, sameBetragInverted));
		Zahlungsposition notInvertedZahlungsposition = createZahlungsposition("11111", sameAb, sameBis,
			sameBgPensum, sameBetragInverted);
		zahlungspositionen.add(notInvertedZahlungsposition);
		zahlungspositionen.add(createZahlungsposition(sameBG, sameAb, sameBis, sameBgPensum, BigDecimal.valueOf(1)));

		ZahlungAuftragDetailsExcelConverter converter = new ZahlungAuftragDetailsExcelConverter();
		List<Zahlungsposition> filteredList = converter.filterZahlungspositionenMitSummeUngleich0(zahlungspositionen);
		Assert.assertEquals(2, filteredList.size());
		Assert.assertEquals(notInvertedZahlungsposition, filteredList.get(0));
	}

	private Zahlungsposition createZahlungsposition(String bgNummer, LocalDate ab, LocalDate bis, BigDecimal bgPensum, BigDecimal betragCHF) {
		Zahlungsposition zahlungsposition = EasyMock.createMock(Zahlungsposition.class);
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = EasyMock.createMock(VerfuegungZeitabschnitt.class);
		Verfuegung verfuegung = EasyMock.createMock(Verfuegung.class);
		Betreuung betreuung = EasyMock.createMock(Betreuung.class);
		EasyMock.expect(betreuung.getBGNummer()).andStubReturn(bgNummer);
		EasyMock.expect(verfuegung.getBetreuung()).andStubReturn(betreuung);
		EasyMock.expect(verfuegungZeitabschnitt.getVerfuegung()).andStubReturn(verfuegung);
		EasyMock.expect(zahlungsposition.getVerfuegungZeitabschnitt()).andStubReturn(verfuegungZeitabschnitt);
		DateRange dateRange = EasyMock.createMock(DateRange.class);
		EasyMock.expect(dateRange.getGueltigAb()).andStubReturn(ab);
		EasyMock.expect(dateRange.getGueltigBis()).andStubReturn(bis);
		EasyMock.expect(verfuegungZeitabschnitt.getGueltigkeit()).andStubReturn(dateRange);
		EasyMock.expect(verfuegungZeitabschnitt.getBgPensum()).andStubReturn(bgPensum);
		EasyMock.expect(zahlungsposition.getBetrag()).andStubReturn(betragCHF);
		EasyMock.expect(zahlungsposition.getStatus()).andStubReturn(ZahlungspositionStatus.KORREKTUR);
		EasyMock.replay(zahlungsposition, verfuegungZeitabschnitt, verfuegung, betreuung, dateRange);
		return zahlungsposition;
	}
}
