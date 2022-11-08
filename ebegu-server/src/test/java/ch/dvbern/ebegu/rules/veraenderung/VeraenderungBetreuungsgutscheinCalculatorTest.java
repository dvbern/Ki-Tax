package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
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


}
