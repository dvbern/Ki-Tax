package ch.dvbern.ebegu.services.util;

import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.test.ZahlungsauftragBuilder;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Test;

public class ZahlungslaufUtilTest {

	@Test
	public void isZahlunglaufRepetition() {
		Zahlungsauftrag first = ZahlungsauftragBuilder.create(builder -> builder
			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 15))
			.withGueltigkeit(new DateRange(
				LocalDate.of(2022, Month.AUGUST, 1),
				LocalDate.of(2022, Month.AUGUST, 31))));
		Assert.assertFalse(
			"Dies ist der erste Zahlungslauf ueberhaupt. Darf keine Repetition sein",
			ZahlungslaufUtil.isZahlunglaufRepetition(first.getGueltigkeit().getGueltigBis(), null));

		Zahlungsauftrag secondInAugust = ZahlungsauftragBuilder.create(builder -> builder
			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 16))
			.withGueltigkeit(new DateRange(
				LocalDate.of(2022, Month.AUGUST, 1),
				LocalDate.of(2022, Month.AUGUST, 31))));
		Assert.assertTrue(
			"Repetition, gleicher Monat",
			ZahlungslaufUtil.isZahlunglaufRepetition(secondInAugust.getGueltigkeit().getGueltigBis(), first));

		Zahlungsauftrag firstInSeptember = ZahlungsauftragBuilder.create(builder -> builder
			.withDatumGeneriert(LocalDate.of(2022, Month.SEPTEMBER, 16))
			.withGueltigkeit(new DateRange(
				LocalDate.of(2022, Month.SEPTEMBER, 1),
				LocalDate.of(2022, Month.SEPTEMBER, 30))));
		Assert.assertFalse(
			"Neuer Monat, erster Zahlungslauf",
			ZahlungslaufUtil.isZahlunglaufRepetition(firstInSeptember.getGueltigkeit().getGueltigBis(), secondInAugust));
	}

	@Test
	public void ermittleZahlungslaufGueltigBis() {
		Zahlungsauftrag auftragAnfangMonat = ZahlungsauftragBuilder.create(builder -> builder
			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 1)));
		Zahlungsauftrag auftragMitteMonat = ZahlungsauftragBuilder.create(builder -> builder
			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 15)));
		Zahlungsauftrag auftragEndeMonat = ZahlungsauftragBuilder.create(builder -> builder
			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 31)));
		Assert.assertEquals(
			LocalDate.of(2022, Month.AUGUST, 31),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(auftragAnfangMonat, 0));
		Assert.assertEquals(
			LocalDate.of(2022, Month.AUGUST, 31),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(auftragMitteMonat, 0));
		Assert.assertEquals(
			LocalDate.of(2022, Month.AUGUST, 31),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(auftragEndeMonat, 0));
		Assert.assertEquals(
			LocalDate.of(2022, Month.SEPTEMBER, 30),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(auftragAnfangMonat, 1));
		Assert.assertEquals(
			LocalDate.of(2022, Month.SEPTEMBER, 30),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(auftragMitteMonat, 1));
		Assert.assertEquals(
			LocalDate.of(2022, Month.SEPTEMBER, 30),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(auftragEndeMonat, 1));
	}

	@Test
	public void ermittleZahlungslaufGueltigVon() {
		LocalDate gueltigBisFirst = LocalDate.of(2022, Month.AUGUST, 31);
		LocalDate expectedGueltigVonFirst = Constants.START_OF_DATETIME.toLocalDate();
		Assert.assertEquals(
			"Erster Zahlungslauf ist ab Beginn der Zeit gültig",
			expectedGueltigVonFirst,
			ZahlungslaufUtil.ermittleZahlungslaufGueltigVon(gueltigBisFirst, null)
		);
		Zahlungsauftrag first = ZahlungsauftragBuilder.create(builder -> builder
			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 15))
			.withGueltigkeit(new DateRange(expectedGueltigVonFirst, gueltigBisFirst)));

		LocalDate gueltigBisSecondInAugust = LocalDate.of(2022, Month.AUGUST, 31);
		LocalDate expectedGueltigVonSecondInAugust = LocalDate.of(2022, Month.AUGUST, 1);
		Assert.assertEquals(
			"Repetition, Beginn des (letzter) Monat der Gueltigkeit des letzten Zahlungslaufs",
			expectedGueltigVonSecondInAugust,
			ZahlungslaufUtil.ermittleZahlungslaufGueltigVon(gueltigBisSecondInAugust, first)
		);
		Zahlungsauftrag secondInAugust = ZahlungsauftragBuilder.create(builder -> builder
			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 16))
			.withGueltigkeit(new DateRange(expectedGueltigVonSecondInAugust, gueltigBisSecondInAugust)));

		LocalDate gueltigBis = LocalDate.of(2022, Month.SEPTEMBER, 30);
		LocalDate expectedGueltigVon = LocalDate.of(2022, Month.SEPTEMBER, 1);
		Assert.assertEquals(
			"Repetition, Beginn des (letzter) Monat der Gueltigkeit des letzten Zahlungslaufs",
			expectedGueltigVon,
			ZahlungslaufUtil.ermittleZahlungslaufGueltigVon(gueltigBis, secondInAugust)
		);
	}
}
