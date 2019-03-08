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

package ch.dvbern.ebegu.tests.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import javax.inject.Inject;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.tests.AbstractEbeguLoginTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
import ch.dvbern.ebegu.util.RuleUtil;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests fuer FinanzielleSituationRechner
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class FinanzielleSituationRechnerTest extends AbstractEbeguLoginTest {

	private static final BigDecimal EINKOMMEN_FINANZIELLE_SITUATION = new BigDecimal("100000");
	private static final BigDecimal EINKOMMEN_EKV_ABGELEHNT = new BigDecimal("80000");
	private static final BigDecimal EINKOMMEN_EKV_ANGENOMMEN = new BigDecimal("79990");
	private static final BigDecimal EINKOMMEN_EKV_ANGENOMMEN_2 = new BigDecimal("79000");

	@Inject
	private FinanzielleSituationRechner finSitRechner;

	@Test
	public void testPositiverDurschnittlicherGewinn() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		LocalDate bis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		TestDataUtil.calculateFinanzDaten(gesuch);

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
		TestDataUtil.calculateFinanzDaten(gesuch);

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
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ABGELEHNT, true);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr()); // Abgelehnt
		Assert.assertNotNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, true);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		LocalDate datumEkv1 = gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1();
		Assert.assertNotNull(datumEkv1);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_1, datumEkv1);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_1_GUELTIG, RuleUtil.getStichtagForEreignis(datumEkv1));
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt2017Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ABGELEHNT, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, false);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr()); // Abgelehnt
		Assert.assertNotNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		LocalDate datumEkv2 = gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2();
		Assert.assertNotNull(datumEkv2);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_2, datumEkv2);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_2_GUELTIG, RuleUtil.getStichtagForEreignis(datumEkv2));
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen2017Abgelehnt() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, gesuch.getGesuchsteller1(), EINKOMMEN_EKV_ANGENOMMEN, false); // nicht weniger als im vorherigen Jahr
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		LocalDate datumEkv1 = gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1();
		Assert.assertNotNull(datumEkv1);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_1, datumEkv1);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_1_GUELTIG, RuleUtil.getStichtagForEreignis(datumEkv1));
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		LocalDate datumEkv2 = gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2();
		Assert.assertNotNull(datumEkv2);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_2, datumEkv2);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_2_GUELTIG, RuleUtil.getStichtagForEreignis(datumEkv2));
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
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		LocalDate datumEkv1 = gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1();
		Assert.assertNotNull(datumEkv1);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_1, datumEkv1);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_1_GUELTIG, RuleUtil.getStichtagForEreignis(datumEkv1));
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN_2, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		LocalDate datumEkv2 = gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2();
		Assert.assertNotNull(datumEkv2);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_2, datumEkv2);
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_2_GUELTIG, RuleUtil.getStichtagForEreignis(datumEkv2));
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
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr()); // Abgelehnt
		Assert.assertNotNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());
		Assert.assertFalse(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr()); // Abgelehnt
		Assert.assertNotNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
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
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setStichtagFuerBasisJahrPlus1(LocalDate.of(2016, Month.DECEMBER, 1));
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjVorAbzFamGr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP1VorAbzFamGr());
		LocalDate datumEkv1 = gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1();
		Assert.assertNotNull(datumEkv1);
		Assert.assertEquals(LocalDate.of(2016, Month.DECEMBER, 1), datumEkv1);
		Assert.assertEquals(LocalDate.of(2017, Month.JANUARY, 1), RuleUtil.getStichtagForEreignis(datumEkv1));
		Assert.assertTrue(gesuch.getFinanzDatenDTO().isEkv1Accepted());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkBjP2VorAbzFamGr());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

}
