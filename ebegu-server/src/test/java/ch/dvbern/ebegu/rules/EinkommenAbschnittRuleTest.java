/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests fuer EinkommenAbschnittRule
 */
public class EinkommenAbschnittRuleTest {

	private static final BigDecimal EINKOMMEN_FINANZIELLE_SITUATION = new BigDecimal("100000.00");
	private static final BigDecimal EINKOMMEN_EKV_ABGELEHNT = new BigDecimal("80001.00");
	private static final BigDecimal EINKOMMEN_EKV_ANGENOMMEN = new BigDecimal("79990.00");
	private static final BigDecimal EINKOMMEN_EKV_ANGENOMMEN_TIEFER = new BigDecimal("60000.00");

	private static final BigDecimal MAX_EINKOMMEN = new BigDecimal("159000.00");
	private static final BigDecimal MAX_EINKOMMEN_EKV = null;
	private final EinkommenAbschnittRule einkommenAbschnittRule =
		new EinkommenAbschnittRule(Constants.DEFAULT_GUELTIGKEIT, Constants.DEFAULT_LOCALE);
	private EinkommenCalcRule einkommenCalcRule =
		new EinkommenCalcRule(Constants.DEFAULT_GUELTIGKEIT, MAX_EINKOMMEN, MAX_EINKOMMEN_EKV, false, Constants.DEFAULT_LOCALE);

	@Test
	public void testKeineEinkommensverschlechterung() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_FINANZIELLE_SITUATION, null, null);

		Assert.assertEquals(1, zeitabschnitte.size());
		Assert.assertEquals(0, EINKOMMEN_FINANZIELLE_SITUATION.compareTo(zeitabschnitte.get(0).getRelevantBgCalculationInput().getMassgebendesEinkommen()));
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ABGELEHNT, null);

		// Es gibt nur einen Zeitraum, da keine EKV angenommen
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_FINANZIELLE_SITUATION, 2016, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde abgelehnt. Es gilt weiterhin das massgebende Einkommen des Jahres 2016. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist nicht um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse. (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_FINANZIELLE_SITUATION, 2016);
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ANGENOMMEN, null);

		// Es gibt zwei Zeiträume, da die EKV immer nur für das Kalenderjahr gilt! Danach gilt wieder die FinSit!
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_EKV_ANGENOMMEN, 		2017, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_FINANZIELLE_SITUATION, 	2016);
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt2017Angenommen() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ABGELEHNT, EINKOMMEN_EKV_ANGENOMMEN);

		// Es kann maximal 2 Abschnitte geben, da die EKVs immer für das ganze Jahr gelten
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_FINANZIELLE_SITUATION, 	2016, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde abgelehnt. Es gilt weiterhin das massgebende Einkommen des Jahres 2016. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist nicht um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse. (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_EKV_ANGENOMMEN, 		2018, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2018 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen2017Angenommen() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ANGENOMMEN, EINKOMMEN_EKV_ANGENOMMEN);

		// Es kann maximal 2 Abschnitte geben, da die EKVs immer für das ganze Jahr gelten
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_EKV_ANGENOMMEN, 2017, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_EKV_ANGENOMMEN, 2018, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2018 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt2017Abgelehnt() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ABGELEHNT, EINKOMMEN_EKV_ABGELEHNT);

		// Es kann maximal 2 Abschnitte geben, da die EKVs immer für das ganze Jahr gelten
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_FINANZIELLE_SITUATION, 2016, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde abgelehnt. Es gilt weiterhin das massgebende Einkommen des Jahres 2016. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist nicht um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse. (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_FINANZIELLE_SITUATION, 2016, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde abgelehnt. Es gilt weiterhin das massgebende Einkommen des Jahres 2016. Das massgebende Einkommen des Jahres 2018 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist nicht um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse. (Art. 34m Abs. 2 ASIV).");
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen2017Angenommen_2016_tiefer() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ANGENOMMEN_TIEFER, EINKOMMEN_EKV_ANGENOMMEN);

		// Es kann maximal 2 Abschnitte geben, da die EKVs immer für das ganze Jahr gelten
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_EKV_ANGENOMMEN_TIEFER, 	2017, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_EKV_ANGENOMMEN, 		2018, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2018 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen2017Angenommen_2017_tiefer() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ANGENOMMEN, EINKOMMEN_EKV_ANGENOMMEN_TIEFER);

		// Es kann maximal 2 Abschnitte geben, da die EKVs immer für das ganze Jahr gelten
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_EKV_ANGENOMMEN, 		2017, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_EKV_ANGENOMMEN_TIEFER, 	2018, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2018 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEinkommensverschlechterungIstEineMassiveEinkommenserhoehung() {
		BigDecimal EINKOMMEN_TIEF = new BigDecimal("60000");
		BigDecimal EINKOMMEN_HOCH = new BigDecimal("100000");
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_TIEF, EINKOMMEN_HOCH, EINKOMMEN_HOCH);

		// Es kann maximal 2 Abschnitte geben, da die EKVs immer für das ganze Jahr gelten
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_TIEF, 2016, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde abgelehnt. Es gilt weiterhin das massgebende Einkommen des Jahres 2016. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist nicht um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse. (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_TIEF, 2016, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde abgelehnt. Es gilt weiterhin das massgebende Einkommen des Jahres 2016. Das massgebende Einkommen des Jahres 2018 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist nicht um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse. (Art. 34m Abs. 2 ASIV).");
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEKVForHighEinkommen_shouldBeIgnored() {
		var ekvLimit = new BigDecimal("80000");
		initCustomEinkommenCalcRule(ekvLimit);

		BigDecimal EINKOMMEN_TIEF = new BigDecimal("60000");
		BigDecimal EINKOMMEN_HOCH = new BigDecimal("100000");
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_HOCH, EINKOMMEN_TIEF, EINKOMMEN_TIEF);
		final String formatedYear = "2016";
		final String EXPECTED_MESSAGE =
			"Ihr Antrag wegen Einkommensverschlechterung wurde abgelehnt. Es gilt weiterhin das massgebende Einkommen des Jahres "
				+ formatedYear
				+ ". Das massgebende Einkommen des Jahres "
				+ formatedYear
				+ " ist höher als CHF "
				+ NumberFormat.getInstance().format(ekvLimit)
				+ " (Art. 57 Abs 2  und Art. 66 Abs. 1 Bst. k FKJV).";

		// Es kann maximal 2 Abschnitte geben, da die EKVs immer für das ganze Jahr gelten
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_HOCH, 2016, EXPECTED_MESSAGE);
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_HOCH, 2016, EXPECTED_MESSAGE);
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	@Test
	public void testEKVForSmallEinkommen_shouldNotBeIgnored() {
		initCustomEinkommenCalcRule(new BigDecimal("80000"));

		BigDecimal EINKOMMEN_MITTEL = new BigDecimal("80000");
		BigDecimal EINKOMMEN_TIEF = new BigDecimal("60000");
		List<VerfuegungZeitabschnitt> zeitabschnitte = createTestdataEinkommensverschlechterung(EINKOMMEN_MITTEL, EINKOMMEN_TIEF, EINKOMMEN_TIEF);

		// Es kann maximal 2 Abschnitte geben, da die EKVs immer für das ganze Jahr gelten
		ExpectedResult jahr1 = new ExpectedResult(EINKOMMEN_TIEF, 2017, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2017 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		ExpectedResult jahr2 = new ExpectedResult(EINKOMMEN_TIEF, 2018, "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres 2018 ohne Abzug des Pauschalbetrags gemäss Familiengrösse ist um mehr als 20 Prozent tiefer als das massgebende Einkommen des aktuellen Bemessungszeitraums (Jahr 2016) ohne Abzug des Pauschalbetrags gemäss Familiengrösse (Art. 34m Abs. 2 ASIV).");
		assertEkvResultate(zeitabschnitte, jahr1, jahr2);
	}

	private void assertEkvResultate(List<VerfuegungZeitabschnitt> zeitabschnitte, ExpectedResult... expectedResults) {
		Assert.assertEquals(expectedResults.length, zeitabschnitte.size());
		int i = 0;
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			verfuegungZeitabschnitt.getBgCalculationResultAsiv().roundAllValues();
			ExpectedResult expectedResult = expectedResults[i++];
			Assert.assertTrue(MathUtil.isSame(expectedResult.massgebendesEinkommen, verfuegungZeitabschnitt.getRelevantBgCalculationInput().getMassgebendesEinkommen()));
			Assert.assertEquals(expectedResult.einkommensjahr, verfuegungZeitabschnitt.getRelevantBgCalculationInput().getEinkommensjahr());
			if(ArrayUtils.isEmpty(expectedResult.bemerkungen)) {
				Assert.assertTrue(verfuegungZeitabschnitt.getVerfuegungZeitabschnittBemerkungList().isEmpty());
			} else {
				Assert.assertEquals(expectedResult.bemerkungen.length, verfuegungZeitabschnitt.getVerfuegungZeitabschnittBemerkungList().size());
				for (int j = 0; j < expectedResult.bemerkungen.length; j++) {
					Assert.assertEquals(expectedResult.bemerkungen[j], verfuegungZeitabschnitt.getVerfuegungZeitabschnittBemerkungList().get(j).getBemerkung());
				}
			}
		}
	}

	@SuppressWarnings("PublicField")
	static class ExpectedResult {
		public BigDecimal massgebendesEinkommen;
		public Integer einkommensjahr;
		public String[] bemerkungen;

		public ExpectedResult(BigDecimal massgebendesEinkommen, Integer einkommensjahr, String... bemerkungen) {
			this.massgebendesEinkommen = massgebendesEinkommen;
			this.einkommensjahr = einkommensjahr;
			this.bemerkungen = bemerkungen;
		}
	}

	private List<VerfuegungZeitabschnitt> createTestdataEinkommensverschlechterung(@Nonnull BigDecimal basisjahr, @Nullable BigDecimal ekv1, @Nullable BigDecimal ekv2) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, basisjahr);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		if (ekv1 != null) {
			TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), ekv1, true);
		}
		if (ekv2 != null) {
			TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), ekv2, false);
		}
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		List<VerfuegungZeitabschnitt> zeitabschnitte = einkommenAbschnittRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		zeitabschnitte = einkommenCalcRule.calculate(betreuung, zeitabschnitte);
		Assert.assertNotNull(zeitabschnitte);
		BemerkungsMerger.prepareGeneratedBemerkungen(zeitabschnitte, gesuch.extractMandant());
		return zeitabschnitte;
	}

	private void initCustomEinkommenCalcRule(@Nullable BigDecimal maxEinkommenEkv) {
		this.einkommenCalcRule = new EinkommenCalcRule(Constants.DEFAULT_GUELTIGKEIT, MAX_EINKOMMEN, maxEinkommenEkv, false, Constants.DEFAULT_LOCALE);
	}
}
