/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;
import java.time.LocalDate;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests fuer FinanzielleSituationBernRechner
 */
public class FinanzielleSituationBernRechnerTest {

	private static final BigDecimal EINKOMMEN_FINANZIELLE_SITUATION = new BigDecimal("100000");
	private static final BigDecimal EINKOMMEN_EKV_ABGELEHNT = new BigDecimal("80001");
	private static final BigDecimal EINKOMMEN_EKV_ANGENOMMEN = new BigDecimal("79990");
	private static final BigDecimal EINKOMMEN_EKV_ANGENOMMEN_2 = new BigDecimal("79000");

	protected AbstractFinanzielleSituationRechner finSitRechner;

	@Before
	public void setUp() {
		finSitRechner = new FinanzielleSituationBernRechner();
	}


	@Test
	public void testPositiverDurschnittlicherGewinn() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		LocalDate bis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();

		//positiv value
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		Assert.assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(100));
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(-100));
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setGeschaeftsgewinnBasisjahrMinus2(BigDecimal.valueOf(300));
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		FinanzielleSituationResultateDTO finSitResultateDTO1 = finSitRechner
			.calculateResultateFinanzielleSituation(gesuch, familiensituation.hasSecondGesuchsteller(bis));

		Assert.assertEquals(BigDecimal.valueOf(100), finSitResultateDTO1.getGeschaeftsgewinnDurchschnittGesuchsteller1());
	}

	@Test
	public void testNegativerDurschnittlicherGewinn() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		LocalDate bis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();

		//negativ value
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		Assert.assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(-100));
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(-100));
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setGeschaeftsgewinnBasisjahrMinus2(BigDecimal.valueOf(-300));
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		FinanzielleSituationResultateDTO finSitResultateDTO2 = finSitRechner
			.calculateResultateFinanzielleSituation(gesuch, familiensituation.hasSecondGesuchsteller(bis));

		Assert.assertEquals(BigDecimal.ZERO, finSitResultateDTO2.getGeschaeftsgewinnDurchschnittGesuchsteller1());
	}

	@Test
	public void testKeineEinkommensverschlechterung() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Erfasst());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ABGELEHNT, true);
		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		BigDecimal differenzJahr1 = FinanzielleSituationBernRechner.calculateProzentualeDifferenz(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ABGELEHNT);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals("EKV abgelehnt, Differenz " + differenzJahr1,
			EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr()); // Abgelehnt
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Erfasst());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals("Keine EVK 2 erfasst", EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, true);
		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Erfasst());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt2017Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ABGELEHNT, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, false);
		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		BigDecimal differenzJahr1 = FinanzielleSituationBernRechner.calculateProzentualeDifferenz(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ABGELEHNT);
		BigDecimal differenzJahr2 = FinanzielleSituationBernRechner.calculateProzentualeDifferenz(EINKOMMEN_FINANZIELLE_SITUATION, EINKOMMEN_EKV_ANGENOMMEN);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals("Differenz: " + differenzJahr1, EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr()); // Abgelehnt
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Erfasst());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals("Differenz: " + differenzJahr2, EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen2017Abgelehnt() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ABGELEHNT, false);
		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Erfasst());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv2Accepted());
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen2017Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN_2, false);
		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Erfasst());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN_2, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv2Accepted());
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt2017Abgelehnt() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ABGELEHNT, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ABGELEHNT, false);
		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr()); // Abgelehnt
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Erfasst());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr()); // Abgelehnt
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv2Accepted());
	}

	@Test
	public void testEinkommensverschlechterungDezember2016Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, true);
		Assert.assertNotNull(gesuch.getEinkommensverschlechterungInfoContainer());
		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Erfasst());
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
	}

	@Test
	public void testGesuchWithoutEKVContainer_shouldIgnoreEKV() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, true);

		gesuch.setEinkommensverschlechterungInfoContainer(null);
		Assert.assertNull(gesuch.getEinkommensverschlechterungInfoContainer());

		TestDataUtil.calculateFinanzDaten(gesuch, finSitRechner);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Erfasst());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv2Erfasst());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv2Accepted());
	}

	@Test
	public void getCalculatedProzentualeDifferenzRounded() {
		Assert.assertEquals(MathUtil.GANZZAHL.from(-19),
			FinanzielleSituationBernRechner.getCalculatedProzentualeDifferenzRounded(BigDecimal.valueOf(181760), BigDecimal.valueOf(146874)));
		Assert.assertEquals(MathUtil.GANZZAHL.from(-22),
			FinanzielleSituationBernRechner.getCalculatedProzentualeDifferenzRounded(BigDecimal.valueOf(181760), BigDecimal.valueOf(140668)));
		Assert.assertEquals(MathUtil.GANZZAHL.from(-20),
			FinanzielleSituationBernRechner.getCalculatedProzentualeDifferenzRounded(BigDecimal.valueOf(181760), BigDecimal.valueOf(144336)));
	}

	@Test
	public void calculateProzentualeDifferenz() {
		Assert.assertEquals(-19.193d,
			FinanzielleSituationBernRechner.calculateProzentualeDifferenz(BigDecimal.valueOf(181760), BigDecimal.valueOf(146874)).doubleValue(), 0.01d);
		Assert.assertEquals(-22.6d,
			FinanzielleSituationBernRechner.calculateProzentualeDifferenz(BigDecimal.valueOf(181760), BigDecimal.valueOf(140668)).doubleValue(), 0.01d);
		Assert.assertEquals(-20.58d,
			FinanzielleSituationBernRechner.calculateProzentualeDifferenz(BigDecimal.valueOf(181760), BigDecimal.valueOf(144336)).doubleValue(), 0.01d);
	}
}
