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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkung;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Fachstelle;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Testet die MaximalesEinkommen-Regel
 */
public class EinkommenCalcRuleTest {

	@Test
	public void testKitaNormalfall() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, new BigDecimal(1000)));

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, (new BigDecimal("50000.00")).compareTo(result.get(0).getMassgebendesEinkommen()));
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		assertFalse(result.get(0).isBezahltVollkosten());
		assertFalse(result.get(0).getBemerkungenMap().isEmpty());
		assertEquals(1, result.get(0).getBemerkungenMap().size());
		assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
	}

	@Test
	public void testKitaEinkommenZuHoch() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(MathUtil.DEFAULT.from(180000),
			BetreuungsangebotTyp.KITA, 100, new BigDecimal(1000)));

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, (new BigDecimal("180000.00")).compareTo(result.get(0).getMassgebendesEinkommen()));
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		assertFalse(result.get(0).isBezahltVollkosten());
		assertFalse(result.get(0).getBemerkungenMap().isEmpty());
		assertEquals(2, result.get(0).getBemerkungenMap().size());
		assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.EINKOMMEN_MSG));
		assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
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
		assertNotNull(gesuch.getEinkommensverschlechterungInfoContainer());
		gesuch.getEinkommensverschlechterungInfoContainer().setEinkommensverschlechterungInfoJA(einkommensverschlechterungInfoJA);

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(new BigDecimal(50000));
		TestDataUtil.calculateFinanzDaten(gesuch);

		gesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		final Einkommensverschlechterung ekvJABasisJahrPlus1 = new Einkommensverschlechterung();
		ekvJABasisJahrPlus1.setNettolohn(new BigDecimal(25000));
		assertNotNull(gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer());
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(ekvJABasisJahrPlus1);
		final Einkommensverschlechterung ekvJABasisJahrPlus2 = new Einkommensverschlechterung();
		ekvJABasisJahrPlus2.setNettolohn(new BigDecimal(20000));
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(ekvJABasisJahrPlus2);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(2, result.size());

		VerfuegungZeitabschnitt abschnittErstesHalbjahrEKV1 = result.get(0);
		assertEquals(25000, abschnittErstesHalbjahrEKV1.getMassgebendesEinkommen().intValue());
		Map<MsgKey, VerfuegungsBemerkung> bemerkungenAbschnitt2 = abschnittErstesHalbjahrEKV1.getBemerkungenMap();
		assertNotNull(bemerkungenAbschnitt2);
		assertEquals(2, bemerkungenAbschnitt2.size());
		assertTrue(bemerkungenAbschnitt2.containsKey(MsgKey.BETREUUNGSANGEBOT_MSG));
		assertTrue(bemerkungenAbschnitt2.containsKey(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG));
		String bemerkungEKV1 = "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres "
			+ TestDataUtil.PERIODE_JAHR_1;
		assertTrue(bemerkungenAbschnitt2.get(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG).getTranslated().contains(bemerkungEKV1));

		VerfuegungZeitabschnitt abschnittZweitesHalbjahrEKV1 = result.get(1);
		assertEquals(20000, abschnittZweitesHalbjahrEKV1.getMassgebendesEinkommen().intValue());
		Map<MsgKey, VerfuegungsBemerkung> bemerkungenAbschnitt3 = abschnittZweitesHalbjahrEKV1.getBemerkungenMap();
		assertNotNull(bemerkungenAbschnitt3);
		assertEquals(2, bemerkungenAbschnitt3.size());
		assertTrue(bemerkungenAbschnitt3.containsKey(MsgKey.BETREUUNGSANGEBOT_MSG));
		assertTrue(bemerkungenAbschnitt3.containsKey(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG));
		String bemerkungEKV2 = "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres "
			+ TestDataUtil.PERIODE_JAHR_2;
		assertTrue(bemerkungenAbschnitt3.get(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG).getTranslated().contains(bemerkungEKV2));
	}

	@Test
	public void sozialhilfebezueger() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(MathUtil.DEFAULT.from(180000),
			BetreuungsangebotTyp.KITA, 100, new BigDecimal(1000), true, false));

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, (new BigDecimal("0.00")).compareTo(result.get(0).getMassgebendesEinkommen()));
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		assertFalse(result.get(0).isBezahltVollkosten());
		assertFalse(result.get(0).getBemerkungenMap().isEmpty());
		assertEquals(2, result.get(0).getBemerkungenMap().size());
		assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG));
		assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
	}

	@Test
	public void nurPauschaleFuerErweiterteBeduernisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(MathUtil.DEFAULT.from(180000),
			BetreuungsangebotTyp.KITA, 100, new BigDecimal(1000), false, true));

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, (new BigDecimal("159000.00")).compareTo(result.get(0).getMassgebendesEinkommen()));
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		assertFalse(result.get(0).isBezahltVollkosten());
		assertFalse(result.get(0).getBemerkungenMap().isEmpty());
		assertEquals(3, result.get(0).getBemerkungenMap().size());
		assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.EINKOMMEN_MSG));
		assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(result.get(0).getBemerkungenMap().containsKey(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG));
	}

	private Betreuung prepareData(BigDecimal massgebendesEinkommen, BetreuungsangebotTyp angebot, int pensum, BigDecimal monatlicheVollkosten) {
		return prepareData(massgebendesEinkommen, angebot, pensum, monatlicheVollkosten, false, false);
	}

	private Betreuung prepareData(BigDecimal massgebendesEinkommen, BetreuungsangebotTyp angebot, int pensum, BigDecimal monatlicheVollkosten,
		boolean sozialhilfeempfaenger, boolean nurZuschlagFuerBesondereBeduerfnisse) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, angebot, pensum, monatlicheVollkosten);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		TestDataUtil.calculateFinanzDaten(gesuch);
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assert.assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(massgebendesEinkommen);
		if (sozialhilfeempfaenger) {
			Objects.requireNonNull(gesuch.extractFamiliensituation()).setSozialhilfeBezueger(true);
		}
		if (nurZuschlagFuerBesondereBeduerfnisse) {
			betreuung.setErweiterteBetreuungContainer(new ErweiterteBetreuungContainer());
			betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(new ErweiterteBetreuung());
			ErweiterteBetreuung erweiterteBetreuungJA = betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
			Objects.requireNonNull(erweiterteBetreuungJA);
			erweiterteBetreuungJA.setErweiterteBeduerfnisse(true);
			erweiterteBetreuungJA.setFachstelle(new Fachstelle());
			erweiterteBetreuungJA.setErweiterteBeduerfnisseBestaetigt(true);
			erweiterteBetreuungJA.setKeineKesbPlatzierung(true);
			Objects.requireNonNull(gesuch.extractFamiliensituation()).setVerguenstigungGewuenscht(false);
		}
		return betreuung;
	}
}
