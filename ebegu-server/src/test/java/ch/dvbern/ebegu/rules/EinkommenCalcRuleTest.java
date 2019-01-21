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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkung;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testet die MaximalesEinkommen-Regel
 */
public class EinkommenCalcRuleTest {

	@Test
	public void testKitaNormalfall() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, new BigDecimal(1000)));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, (new BigDecimal("50000.00")).compareTo(result.get(0).getMassgebendesEinkommen()));
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).isBezahltVollkosten());
		Assert.assertFalse(result.get(0).getBemerkungenMap().isEmpty());
		Assert.assertEquals(1, result.get(0).getBemerkungenMap().size());
		Assert.assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
	}

	@Test
	public void testKitaEinkommenZuHoch() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(MathUtil.DEFAULT.from(180000),
			BetreuungsangebotTyp.KITA, 100, new BigDecimal(1000)));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, (new BigDecimal("180000.00")).compareTo(result.get(0).getMassgebendesEinkommen()));
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).isBezahltVollkosten());
		Assert.assertFalse(result.get(0).getBemerkungenMap().isEmpty());
		Assert.assertEquals(2, result.get(0).getBemerkungenMap().size());
		Assert.assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.EINKOMMEN_MSG));
		Assert.assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
	}

	/**
	 * Erstellt einen Testfall mit 2 EKV.
	 * Am Ende schaut es dass die Bemerkungen richtig geschrieben wurden
	 */
	@Test
	public void testAcceptedEKV() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.TAGESSCHULE, 100, new BigDecimal(1000));
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);

		gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
		final EinkommensverschlechterungInfo einkommensverschlechterungInfoJA = new EinkommensverschlechterungInfo();
		einkommensverschlechterungInfoJA.setEinkommensverschlechterung(true);
		einkommensverschlechterungInfoJA.setEkvFuerBasisJahrPlus1(true);
		einkommensverschlechterungInfoJA.setEkvFuerBasisJahrPlus2(true);
		einkommensverschlechterungInfoJA.setStichtagFuerBasisJahrPlus1(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, 10, 1));
		einkommensverschlechterungInfoJA.setStichtagFuerBasisJahrPlus2(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 4, 1));
		gesuch.getEinkommensverschlechterungInfoContainer().setEinkommensverschlechterungInfoJA(einkommensverschlechterungInfoJA);

		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(new BigDecimal(50000));
		TestDataUtil.calculateFinanzDaten(gesuch);

		gesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		final Einkommensverschlechterung ekvJABasisJahrPlus1 = new Einkommensverschlechterung();
		ekvJABasisJahrPlus1.setNettolohnJan(new BigDecimal(25000));
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(ekvJABasisJahrPlus1);
		final Einkommensverschlechterung ekvJABasisJahrPlus2 = new Einkommensverschlechterung();
		ekvJABasisJahrPlus2.setNettolohnJan(new BigDecimal(20000));
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(ekvJABasisJahrPlus2);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		Assert.assertEquals(3, result.size());

		Assert.assertEquals(new BigDecimal(50000), result.get(0).getMassgebendesEinkommen());
		Map<MsgKey, VerfuegungsBemerkung> bemerkungenAbschnitt1 = result.get(0).getBemerkungenMap();
		Assert.assertNotNull(bemerkungenAbschnitt1);
		Assert.assertEquals(1, bemerkungenAbschnitt1.size());
		Assert.assertTrue(bemerkungenAbschnitt1.containsKey(MsgKey.BETREUUNGSANGEBOT_MSG));

		Assert.assertEquals(new BigDecimal(25000), result.get(1).getMassgebendesEinkommen());
		Map<MsgKey, VerfuegungsBemerkung> bemerkungenAbschnitt2 = result.get(1).getBemerkungenMap();
		Assert.assertNotNull(bemerkungenAbschnitt2);
		Assert.assertEquals(2, bemerkungenAbschnitt2.size());
		Assert.assertTrue(bemerkungenAbschnitt2.containsKey(MsgKey.BETREUUNGSANGEBOT_MSG));
		Assert.assertTrue(bemerkungenAbschnitt2.containsKey(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG));
		String bemerkungEKV1 = "Die Bemessung erfolgt auf dem provisorischen Einkommen des Jahres " + TestDataUtil.PERIODE_JAHR_1;
		Assert.assertTrue(bemerkungenAbschnitt2.get(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG).getTranslated().contains(bemerkungEKV1));

		Assert.assertEquals(new BigDecimal(20000), result.get(2).getMassgebendesEinkommen());
		Map<MsgKey, VerfuegungsBemerkung> bemerkungenAbschnitt3 = result.get(2).getBemerkungenMap();
		Assert.assertNotNull(bemerkungenAbschnitt3);
		Assert.assertEquals(2, bemerkungenAbschnitt3.size());
		Assert.assertTrue(bemerkungenAbschnitt3.containsKey(MsgKey.BETREUUNGSANGEBOT_MSG));
		Assert.assertTrue(bemerkungenAbschnitt3.containsKey(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG));
		String bemerkungEKV2 = "Die Bemessung erfolgt auf dem provisorischen Einkommen des Jahres " + TestDataUtil.PERIODE_JAHR_2;
		Assert.assertTrue(bemerkungenAbschnitt3.get(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG).getTranslated().contains(bemerkungEKV2));
	}

	private Betreuung prepareData(BigDecimal massgebendesEinkommen, BetreuungsangebotTyp angebot, int pensum, BigDecimal monatlicheVollkosten) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, angebot, pensum, monatlicheVollkosten);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(massgebendesEinkommen);
		return betreuung;
	}
}
