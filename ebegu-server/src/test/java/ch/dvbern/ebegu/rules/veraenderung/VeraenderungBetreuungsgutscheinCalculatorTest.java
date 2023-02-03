package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.Assert;
import org.junit.Test;


public class VeraenderungBetreuungsgutscheinCalculatorTest {

	private DateRange august = new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.START_PERIODE.with(TemporalAdjusters.lastDayOfMonth()));
	private DateRange september = new DateRange(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.START_PERIODE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
	private DateRange septemberFirstHalf = new DateRange(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.START_PERIODE.plusMonths(1).plusDays(14));
	private DateRange septemberSecondHalf = new DateRange(TestDataUtil.START_PERIODE.plusMonths(1).plusDays(15), TestDataUtil.START_PERIODE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));

	@Test
	public void useCorrectImplementation() {
		VeraenderungCalculator veraenderungCalculator = VeraenderungCalculator.getVeranderungCalculator(false);
		Assert.assertTrue(veraenderungCalculator instanceof  VeraenderungBetreuungsgutscheinCalculator);
	}

	@Test
	public void veraenderung() {
		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(15), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(25), september)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-10), veraenderung);
	}

	@Test
	public void negativeVeraenderungWithOnlyFinSitChangeShouldBeIgnorable() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(15), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(25), september)
		);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-10), veraenderung);
		Assert.assertTrue(VeraenderungCalculator.getVeranderungCalculator(false).calculateIgnorable(zeitaschnitteAktuell, verfuegung, veraenderung));
	}

	@Test
	public void positiveVeraenderungWithOnlyFinSitChangeShouldNotBeIgnorable() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(15), august),
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(25), september)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(10), veraenderung);
		Assert.assertFalse(VeraenderungCalculator.getVeranderungCalculator(false).calculateIgnorable(zeitaschnitteAktuell, verfuegung, veraenderung));
	}

	@Test
	public void positiveVeraenderungWithFinSitAndAnspruchChangeShouldNotBeIgnorable() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		final VerfuegungZeitabschnitt zeitabschnittMitVergunstigung =
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(15), august);
		zeitabschnittMitVergunstigung.getBgCalculationResultAsiv().setAnspruchspensumProzent(20);
		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
				zeitabschnittMitVergunstigung,
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(25), september)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(10), veraenderung);
		Assert.assertFalse(VeraenderungCalculator.getVeranderungCalculator(false).calculateIgnorable(zeitaschnitteAktuell, verfuegung, veraenderung));
	}

	@Test
	public void negativeVeraenderungWithFinSitAndAnspruchChangeShouldNotBeIgnorable() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(15), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(25), september)
		);

		final VerfuegungZeitabschnitt zeitabschnittMitVergunstigung =
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august);
		zeitabschnittMitVergunstigung.getBgCalculationResultAsiv().setAnspruchspensumProzent(20);
		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
				zeitabschnittMitVergunstigung,
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-10), veraenderung);
		Assert.assertFalse(VeraenderungCalculator.getVeranderungCalculator(false).calculateIgnorable(zeitaschnitteAktuell, verfuegung, veraenderung));
	}

	@Test
	public void positiveVeraenderungWithFinSitAndBetreuungChangeShouldNotBeIgnorable() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		final VerfuegungZeitabschnitt zeitabschnittMitVergunstigung =
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(15), august);
		zeitabschnittMitVergunstigung.getBgCalculationResultAsiv().setBetreuungspensumProzent(BigDecimal.valueOf(20));

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
				zeitabschnittMitVergunstigung,
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(25), september)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(10), veraenderung);
		Assert.assertFalse(VeraenderungCalculator.getVeranderungCalculator(false).calculateIgnorable(zeitaschnitteAktuell, verfuegung, veraenderung));
	}

	@Test
	public void noVeraenderungShouldBeIgnorable() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(0), veraenderung);
		Assert.assertTrue(VeraenderungCalculator.getVeranderungCalculator(false).calculateIgnorable(zeitaschnitteAktuell, verfuegung, veraenderung));
	}

	@Test
	public void nonMatchingZeitabschnitteShouldNotBeIgnorable() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), septemberFirstHalf),
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(9), septemberSecondHalf)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-1), veraenderung);
		Assert.assertFalse(VeraenderungCalculator.getVeranderungCalculator(false).calculateIgnorable(zeitaschnitteAktuell, verfuegung, veraenderung));
	}

	@Test
	public void nonMatchingDaysZeitabschnitteShouldNotBeIgnorable() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september)
		);

		DateRange alternativeSeptember = new DateRange(LocalDate.from(september.getGueltigAb()), LocalDate.from(september.getGueltigBis()).minusDays(1));

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august),
				createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), alternativeSeptember)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(0), veraenderung);
		Assert.assertFalse(VeraenderungCalculator.getVeranderungCalculator(false).calculateIgnorable(zeitaschnitteAktuell, verfuegung, veraenderung));
	}

	@Test
	public void ausbezahlteVerguenstigung_noZeitabschnittAusbezahlt() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);
		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(30), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_noVorgaenger() {
		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(30), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, null);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		Assert.assertNull(verfuegung.getKorrekturAusbezahltInstitution());
		Assert.assertNull(verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_ausbeazahltInstitutionen() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), august, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(30), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.valueOf(-20), verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_ausbeazahltInstitutionen_oneMonth() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(50), august, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(30), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.valueOf(30), verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_ignorieren() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(50), august, VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.IGNORIEREND, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(30), september, VerfuegungsZeitabschnittZahlungsstatus.IGNORIEREND, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.valueOf(20), verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_ausbeazahltInstitutionen_multipleZAsAktuell() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(50), august, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10), septemberFirstHalf, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(30), septemberSecondHalf, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.valueOf(10), verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_ausbeazahltInstitutionen_multipleZAsFVorgaenger() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(50), august, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), septemberFirstHalf, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(30), septemberSecondHalf, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(90), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.valueOf(-10), verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_ausbeazahltEltern() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(50), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		zeitaschnitteVorgaenger.forEach(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().setAuszahlungAnEltern(true));
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(90), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		zeitaschnitteAktuell.forEach(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().setAuszahlungAnEltern(true));
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.valueOf(30), verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_ausbeazahltBoth() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(50), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		zeitaschnitteVorgaenger.forEach(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().setAuszahlungAnEltern(true));
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(90), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		zeitaschnitteAktuell.forEach(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().setAuszahlungAnEltern(true));
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.valueOf(-70), verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.valueOf(30), verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_mahlzeiten() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(50), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		zeitaschnitteVorgaenger.forEach(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().setVerguenstigungMahlzeitenTotal(BigDecimal.valueOf(30)));
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(90), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		zeitaschnitteAktuell.forEach(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().setVerguenstigungMahlzeitenTotal(BigDecimal.valueOf(50)));
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.ZERO, verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.valueOf(-20), verfuegung.getKorrekturAusbezahltEltern());
	}

	@Test
	public void ausbezahlteVerguenstigung_mahlzeitenAndInstitutionen() {
		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(50), august, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), september, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		zeitaschnitteVorgaenger.forEach(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().setVerguenstigungMahlzeitenTotal(BigDecimal.valueOf(40)));
		Verfuegung vorgaenger = new Verfuegung();
		vorgaenger.setZeitabschnitte(zeitaschnitteVorgaenger);

		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20), august, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(90), september, VerfuegungsZeitabschnittZahlungsstatus.NEU, VerfuegungsZeitabschnittZahlungsstatus.NEU)
		);
		zeitaschnitteAktuell.forEach(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().setVerguenstigungMahlzeitenTotal(BigDecimal.valueOf(15)));
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteAktuell);

		Betreuung betreuung = initBetreuung(verfuegung, vorgaenger);

		VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateKorrekturAusbezahlteVerguenstigung(betreuung);

		assertEquals(BigDecimal.valueOf(-40), verfuegung.getKorrekturAusbezahltInstitution());
		assertEquals(BigDecimal.valueOf(25), verfuegung.getKorrekturAusbezahltEltern());
	}

	private Betreuung initBetreuung(Verfuegung verfuegung, Verfuegung vorgaenger) {
		if (vorgaenger != null) {
			vorgaenger.setBetreuung(TestDataUtil.createGesuchWithBetreuungspensum(false));
		}

		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
		betreuung.setVerfuegung(verfuegung);
		betreuung.initVorgaengerVerfuegungen(vorgaenger, null);
		verfuegung.setBetreuung(betreuung);
		return betreuung;
	}
	private VerfuegungZeitabschnitt createZeitabschnittMitVergunstigung(
		BigDecimal verguenstiung,
		DateRange gueltigkeit,
		VerfuegungsZeitabschnittZahlungsstatus zahlungStatusInsti,
		VerfuegungsZeitabschnittZahlungsstatus zahlungStatusEltern
		) {
		VerfuegungZeitabschnitt za = createZeitabschnittMitVergunstigung(verguenstiung, gueltigkeit);
		za.setZahlungsstatusInstitution(zahlungStatusInsti);
		za.setZahlungsstatusAntragsteller(zahlungStatusEltern);
		return za;
	}

	private VerfuegungZeitabschnitt createZeitabschnittMitVergunstigung(BigDecimal verguenstiung, DateRange gueltigkeit) {
		BGCalculationResult bgCalculationResult = createDummyBGCalculationResult(verguenstiung);
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setGueltigkeit(gueltigkeit);
		zeitabschnitt.setBgCalculationResultAsiv(bgCalculationResult);
		return zeitabschnitt;
	}

	private BGCalculationResult createDummyBGCalculationResult(BigDecimal verguenstigung) {
		BGCalculationResult bgCalculationResult = new BGCalculationResult();
		bgCalculationResult.setFamGroesse(BigDecimal.valueOf(3));
		bgCalculationResult.setAnspruchspensumProzent(60);
		bgCalculationResult.setBetreuungspensumProzent(BigDecimal.valueOf(60));
		bgCalculationResult.setVerguenstigungMahlzeitenTotal(BigDecimal.valueOf(10));
		bgCalculationResult.setVollkosten(BigDecimal.valueOf(1000));
		bgCalculationResult.setVerguenstigung(verguenstigung);
		bgCalculationResult.setBesondereBeduerfnisseBestaetigt(true);

		return bgCalculationResult;
	}

	private void assertEquals(BigDecimal expected, BigDecimal actual) {
		Assert.assertEquals(expected.stripTrailingZeros(), actual.stripTrailingZeros());
	}


}
