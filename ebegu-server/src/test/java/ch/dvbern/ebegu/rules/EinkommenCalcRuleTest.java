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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkung;
import ch.dvbern.ebegu.dto.VerfuegungsBemerkungList;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Test;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_PAUSCHALE_BEI_ANSPRUCH;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.EINSTELLUNG_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.MAX_EINKOMMEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Testet die MaximalesEinkommen-Regel
 */
public class EinkommenCalcRuleTest {

	private final BigDecimal EINKOMMEN = MathUtil.DEFAULT.fromNullSafe(100000);
	private final BigDecimal EINKOMMEN_HOCH = MathUtil.DEFAULT.fromNullSafe(180000);

	@Test
	public void testNormalfallKita() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungKita(
			EINKOMMEN, false, false, false, FinSitStatus.AKZEPTIERT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals(100, abschnitt.getAnspruchberechtigtesPensum());
		assertFalse(abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertFalse(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testNormalfallTagesschule() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungTagesschule(
			EINKOMMEN, false, false, FinSitStatus.AKZEPTIERT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals(100, abschnitt.getAnspruchberechtigtesPensum());
		assertFalse(abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertFalse(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(1, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.BETREUUNGSANGEBOT_MSG));
	}

	@Test
	public void testEinkommenZuHochKita() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungKita(
			EINKOMMEN_HOCH, false, false, false, FinSitStatus.AKZEPTIERT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, EINKOMMEN_HOCH.compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals(0, abschnitt.getAnspruchberechtigtesPensum());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.EINKOMMEN_MAX_MSG));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
	}

	@Test
	public void testEinkommenZuHochTagesschule() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungTagesschule(
			EINKOMMEN_HOCH, false, false, FinSitStatus.AKZEPTIERT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, EINKOMMEN_HOCH.compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals(100, abschnitt.getAnspruchberechtigtesPensum());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.EINKOMMEN_MAX_MSG));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.BETREUUNGSANGEBOT_MSG));
	}

	/**
	 * Erstellt einen Testfall mit 2 EKV.
	 * Am Ende schaut es dass die Bemerkungen richtig geschrieben wurden
	 */
	@Test
	public void testAcceptedEKV() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, KITA,100, BigDecimal.ZERO);
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
		VerfuegungsBemerkungList bemerkungenAbschnitt2 = abschnittErstesHalbjahrEKV1.getBemerkungenList();
		assertNotNull(bemerkungenAbschnitt2);
		assertEquals(3, bemerkungenAbschnitt2.uniqueSize());
		assertTrue(bemerkungenAbschnitt2.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(bemerkungenAbschnitt2.containsMsgKey(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG));
		assertTrue(bemerkungenAbschnitt2.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
		String bemerkungEKV1 = "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres "
			+ TestDataUtil.PERIODE_JAHR_1;
		VerfuegungsBemerkung bemerkungEkvAccept1 = bemerkungenAbschnitt2.findFirstBemerkungByMsgKey(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG);
		assertNotNull(bemerkungEkvAccept1);
		assertTrue(bemerkungEkvAccept1.getTranslated().contains(bemerkungEKV1));

		VerfuegungZeitabschnitt abschnittZweitesHalbjahrEKV1 = result.get(1);
		assertEquals(20000, abschnittZweitesHalbjahrEKV1.getMassgebendesEinkommen().intValue());
		VerfuegungsBemerkungList bemerkungenAbschnitt3 = abschnittZweitesHalbjahrEKV1.getBemerkungenList();
		assertNotNull(bemerkungenAbschnitt3);
		assertEquals(3, bemerkungenAbschnitt3.uniqueSize());
		assertTrue(bemerkungenAbschnitt3.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(bemerkungenAbschnitt3.containsMsgKey(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG));
		assertTrue(bemerkungenAbschnitt3.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
		String bemerkungEKV2 = "Ihr Antrag zur Anwendung der Einkommensverschlechterung wurde gutgeheissen. Das massgebende Einkommen des Jahres "
			+ TestDataUtil.PERIODE_JAHR_2;
		VerfuegungsBemerkung bemerkungEkvAccept2 = bemerkungenAbschnitt3.findFirstBemerkungByMsgKey(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG);
		assertNotNull(bemerkungEkvAccept2);
		assertTrue(bemerkungEkvAccept2.getTranslated().contains(bemerkungEKV2));
	}

	@Test
	public void testSozialhilfebezueger() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungKita(
			EINKOMMEN, true, false, false, FinSitStatus.AKZEPTIERT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, (new BigDecimal("0.00")).compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals(100, abschnitt.getAnspruchberechtigtesPensum());
		assertFalse(abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertFalse(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(3, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testNurPauschaleFuerErweiterteBeduernisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungKita(
			EINKOMMEN_HOCH, false, true, true, FinSitStatus.AKZEPTIERT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, (new BigDecimal(EINSTELLUNG_MAX_EINKOMMEN)).compareTo(result.get(0).getMassgebendesEinkommen()));
		assertEquals("Anspruch wird wegen Pauschale bes. Bed. nicht auf 0 gesetzt", 100, abschnitt.getAnspruchberechtigtesPensum());
		assertFalse("erweiterteBetreuung: BezahltVollkosten nicht gesetzt", abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(4, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG));
		assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testKeinePauschaleFuerErweiterteBeduernisseWennEinkommenZuHoch() {
		Betreuung betreuung = prepareBetreuungKita(
			EINKOMMEN_HOCH, false, true, true, FinSitStatus.AKZEPTIERT);
		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(FKJV_PAUSCHALE_BEI_ANSPRUCH).setValue("true");
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, (new BigDecimal(EINSTELLUNG_MAX_EINKOMMEN)).compareTo(result.get(0).getMassgebendesEinkommen()));
		assertEquals("Anspruch wird trotz Pauschale bes. Bed. auf 0 gesetzt", 0, abschnitt.getAnspruchberechtigtesPensum());
		assertTrue("erweiterteBetreuung: BezahltVollkosten gesetzt", abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(3, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG));
		assertFalse(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testKeineFinSitErfasstOhneErweiterteBeduerfnisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungKita(
			EINKOMMEN, false, true, false, FinSitStatus.AKZEPTIERT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, MAX_EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals(0, abschnitt.getAnspruchberechtigtesPensum());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
	}

	@Test
	public void testFinSitStatusNullKita() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungKita(
			EINKOMMEN, false, false, false, null));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals(100, abschnitt.getAnspruchberechtigtesPensum());
		assertFalse(abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertFalse(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testFinSitStatusAbgelehntOhneBesondereBeduerfnisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungKita(
			EINKOMMEN, false, false, false, FinSitStatus.ABGELEHNT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, MAX_EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals("Keine erweiterteBetreuung: Anspruch wird auf 0 gesetzt", 0, abschnitt.getAnspruchberechtigtesPensum());
		assertTrue("Keine erweiterteBetreuung: Bezahlt Vollkosten", abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertTrue(abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG));
	}

	@Test
	public void testFinSitStatusAbgelehntMitBesondereBeduerfnisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareBetreuungKita(
			EINKOMMEN, false, false, true, FinSitStatus.ABGELEHNT));

		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertEquals(0, MAX_EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()));
		assertEquals("Anspruch wird wegen Pauschale bes. Bed. nicht auf 0 gesetzt", 100, abschnitt.getAnspruchberechtigtesPensum());
		assertFalse("erweiterteBetreuung: BezahltVollkosten nicht gesetzt", abschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertTrue("keinAnspruchAufgrundEinkommen gilt auch wenn erweiterteBetreuung", abschnitt.getBgCalculationInputAsiv().isKeinAnspruchAufgrundEinkommen());
		assertFalse(abschnitt.getBemerkungenList().isEmpty());
		assertEquals(4, abschnitt.getBemerkungenList().uniqueSize());
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG));
		assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	private Betreuung prepareBetreuungKita(
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean sozialhilfeempfaenger,
		boolean keineVerguenstigungGewuenscht,
		boolean erweiterteBeduerfnisse,
		@Nullable FinSitStatus finSitStatus
	) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, KITA, 100, MathUtil.DEFAULT.fromNullSafe(2000));
		prepareGesuch(betreuung, massgebendesEinkommen, sozialhilfeempfaenger, keineVerguenstigungGewuenscht, finSitStatus);
		if (erweiterteBeduerfnisse) {
			betreuung.setErweiterteBetreuungContainer(new ErweiterteBetreuungContainer());
			betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(new ErweiterteBetreuung());
			ErweiterteBetreuung erweiterteBetreuungJA = betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
			Objects.requireNonNull(erweiterteBetreuungJA);
			erweiterteBetreuungJA.setErweiterteBeduerfnisse(true);
			erweiterteBetreuungJA.setFachstelle(new Fachstelle());
			erweiterteBetreuungJA.setErweiterteBeduerfnisseBestaetigt(true);
			erweiterteBetreuungJA.setKeineKesbPlatzierung(true);
		}
		return betreuung;
	}

	private AnmeldungTagesschule prepareBetreuungTagesschule(
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean sozialhilfeempfaenger,
		boolean keineVerguenstigungGewuenscht,
		@Nullable FinSitStatus finSitStatus
	) {
		final AnmeldungTagesschule anmeldung = TestDataUtil.createGesuchWithAnmeldungTagesschule();
		prepareGesuch(anmeldung, massgebendesEinkommen, sozialhilfeempfaenger, keineVerguenstigungGewuenscht, finSitStatus);
		return anmeldung;
	}

	private void prepareGesuch(
		@Nonnull AbstractPlatz platz,
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean sozialhilfeempfaenger,
		boolean keineVerguenstigungGewuenscht,
		@Nullable FinSitStatus finSitStatus
	) {
		Gesuch gesuch = platz.extractGesuch();
		gesuch.setFinSitStatus(finSitStatus);
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		TestDataUtil.calculateFinanzDaten(gesuch);
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(massgebendesEinkommen);
		if (sozialhilfeempfaenger) {
			Objects.requireNonNull(gesuch.extractFamiliensituation()).setSozialhilfeBezueger(true);
		}
		Objects.requireNonNull(gesuch.extractFamiliensituation()).setVerguenstigungGewuenscht(!keineVerguenstigungGewuenscht);
	}
}
