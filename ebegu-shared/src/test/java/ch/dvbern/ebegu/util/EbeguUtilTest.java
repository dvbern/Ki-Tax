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

package ch.dvbern.ebegu.util;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

/**
 * test fuer Ebeguutil
 */
public class EbeguUtilTest {

	@Test
	public void testFromOneGSToTwoGS_From2To1() {

		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertFalse(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void testFromOneGSToTwoGS_From2To2() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertFalse(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void testFromOneGSToTwoGS_From1To1() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		oldData.setStartKonkubinat(LocalDate.now());

		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertFalse(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void testFromOneGSToTwoGS_From1To2() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertTrue(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void testFromOneGSToTwoGS_nullFamilienstatus() {
		Familiensituation oldData = new Familiensituation();
		Familiensituation newData = new Familiensituation();

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertFalse(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void toFilename() {
		String filename = EbeguUtil.toFilename("Kita Beundenweg/Crèche Oeuches.pdf");
		Assert.assertNotNull(filename);
		Assert.assertEquals("Kita_Beundenweg_Crèche_Oeuches.pdf", filename);
	}

	@Test
	public void isErlaeuterungenZurVerfuegungMit0AnspruchRequiredTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier.BERN, 0);
		// 0 Anspruch sollte keine Erlaeterung sein:
		Assert.assertFalse(EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch));
	}

	@Test
	public void isErlaeuterungenZurVerfuegungMit1AnspruchRequiredTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier.BERN, 1);
		// 0 Anspruch sollte keine Erlaeterung sein:
		Assert.assertTrue(EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch));
	}
	@Test
	public void isErlaeuterungenZurVerfuegungMit0AnspruchRequiredFuerAppenzellTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier.APPENZELL_AUSSERRHODEN, 0);
		// 0 Anspruch aber beim Appenzell ist einer Ausnahme so es muss immer sein:
		Assert.assertTrue(EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch));
	}
	@Test
	public void isErlaeuterungenZurVerfuegungMit1AnspruchRequiredFuerAppenzellTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier.APPENZELL_AUSSERRHODEN, 1);
		Assert.assertTrue(EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch));
	}

	@Test
	public void isErlaeuterungenZurVerfuegungFuerSchwyzRequiredTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier.SCHWYZ, 1);
		// Beim Schwyz sollte immer false sein
		Assert.assertFalse(EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch));
	}

	@Test
	public void isFinanzielleSituationRequiredTest() {
		Gesuch gesuch = new Gesuch();
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationRequired(gesuch));

		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setSozialhilfeBezueger(true);
		FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationRequired(gesuch));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setSozialhilfeBezueger(false);
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setVerguenstigungGewuenscht(false);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationRequired(gesuch));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setVerguenstigungGewuenscht(true);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationRequired(gesuch));
	}

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "BERN","BERN_FKJV"},
		mode = Mode.INCLUDE)
	public void isFinanzielleSituationIntroduceAndComplete_BERN_Test(FinanzielleSituationTyp finanzielleSituationTyp) {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(finanzielleSituationTyp);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());

		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(initFinSitBern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
		gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(initFinSitBern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
	}

	@Test
	public void isFinanzielleSituationIntroducedAndComplete_SOLOTHURN_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.SOLOTHURN);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
		FinanzielleSituation finSitSolothurn = new FinanzielleSituation();

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(finSitSolothurn);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setBruttoLohn(BigDecimal.ZERO);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerbaresVermoegen(BigDecimal.ZERO);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setBruttoLohn(null);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(BigDecimal.ZERO);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setUnterhaltsBeitraege(BigDecimal.ZERO);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setAbzuegeKinderAusbildung(BigDecimal.ZERO);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
	}

	@Test
	public void isFinanzielleSituationIntroducedAndComplete_LUZERN_Infoma_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		gesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(initFinSitLuzern());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(new Familiensituation());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setAuszahlungsdaten(new Auszahlungsdaten());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten().setIban(new IBAN());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten().setInfomaBankcode("test");
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten().setInfomaKreditorennummer("test");
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
	}

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "APPENZELL","APPENZELL_2"},
		mode = Mode.INCLUDE)
	public void isFinanzielleSituationIntroducedAndComplete_AR_Test(FinanzielleSituationTyp finanzielleSituationTyp) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(finanzielleSituationTyp);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(new Familiensituation());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		FinanzielleSituation finSit = new FinanzielleSituation();
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(finSit);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setFinSitZusatzangabenAppenzell(initFinSitAppenzell());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setSteuerbaresVermoegen(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setSteuerbaresEinkommen(BigDecimal.TEN);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
	}

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "APPENZELL","APPENZELL_2"},
		mode = Mode.INCLUDE)
	public void isFinanzielleSituationIntroducedAndComplete_isMandantSpecificFinSitGemeinsam_AR_Test(FinanzielleSituationTyp finanzielleSituationTyp) {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(finanzielleSituationTyp);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(new Familiensituation());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setGemeinsameSteuererklaerung(false);
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		FinanzielleSituation finSit = new FinanzielleSituation();
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(finSit);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setFinSitZusatzangabenAppenzell(initFinSitAppenzell());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setSteuerbaresVermoegen(BigDecimal.TEN);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setSteuerbaresEinkommen(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setGemeinsameSteuererklaerung(true);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
	}

	@Test
	public void isFinanzielleSituationIntroducedAndComplete_isMandantSpecificFinSitGemeinsam_LU_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		gesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(new Familiensituation());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setAuszahlungsdaten(new Auszahlungsdaten());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten().setIban(new IBAN());
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(initFinSitLuzern());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
	}

	@Test
	public void isFinanzielleSituationIntroducedAndComplete_ekvVollstaendig_SO_Test(){
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.SOLOTHURN);
		gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
		gesuch.getEinkommensverschlechterungInfoContainer().setEinkommensverschlechterungInfoJA(new EinkommensverschlechterungInfo());
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus1(true);
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(false);

		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEinkommensverschlechterung(true);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
		gesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().setBruttolohnAbrechnung1(BigDecimal.ONE);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().setBruttolohnAbrechnung2(BigDecimal.ONE);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().setBruttolohnAbrechnung3(BigDecimal.ONE);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().setNettoVermoegen(BigDecimal.ONE);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().setExtraLohn(true);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		gesuch.getGesuchsteller2().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(createEkvSolothurn());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(true);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(createEkvSolothurn());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(createEkvSolothurn());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
	}

	private Einkommensverschlechterung createEkvSolothurn() {
		Einkommensverschlechterung einkommensverschlechterung = new Einkommensverschlechterung();
		einkommensverschlechterung.setBruttolohnAbrechnung1(BigDecimal.ONE);
		einkommensverschlechterung.setBruttolohnAbrechnung2(BigDecimal.ONE);
		einkommensverschlechterung.setBruttolohnAbrechnung3(BigDecimal.ONE);
		einkommensverschlechterung.setExtraLohn(true);
		einkommensverschlechterung.setNettoVermoegen(BigDecimal.ONE);
		return einkommensverschlechterung;
	}

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "BERN","BERN_FKJV"},
		mode = Mode.INCLUDE)
	public void isFinanzielleSituationIntroducedAndComplete_EKV_Vollstaendig_Test(FinanzielleSituationTyp finanzielleSituationTyp){
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(finanzielleSituationTyp);
		gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
		gesuch.getEinkommensverschlechterungInfoContainer().setEinkommensverschlechterungInfoJA(new EinkommensverschlechterungInfo());
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus1(true);
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(false);

		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEinkommensverschlechterung(true);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(initEKVBern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		gesuch.getGesuchsteller2().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(initEKVBern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(true);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(initEKVBern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(initEKVBern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
	}

	@Test
	public void isFinanzielleSituationIntroducedAndComplete_EKV_Vollstaendig_LUZERN_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(new Familiensituation());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setAuszahlungsdaten(new Auszahlungsdaten());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten().setIban(new IBAN());
		gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
		gesuch.getEinkommensverschlechterungInfoContainer().setEinkommensverschlechterungInfoJA(new EinkommensverschlechterungInfo());
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus1(true);
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(false);

		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEinkommensverschlechterung(true);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(initEKVLuzern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		gesuch.getGesuchsteller2().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(initEKVLuzern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(true);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(initEKVLuzern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(initEKVLuzern());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
	}

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "APPENZELL","APPENZELL_2"},
		mode = Mode.INCLUDE)
	public void isFinanzielleSituationIntroducedAndComplete_EKV_Vollstaendig_AR_Test(FinanzielleSituationTyp finanzielleSituationTyp) {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(finanzielleSituationTyp);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(new Familiensituation());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setGemeinsameSteuererklaerung(false);
		gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
		gesuch.getEinkommensverschlechterungInfoContainer().setEinkommensverschlechterungInfoJA(new EinkommensverschlechterungInfo());
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus1(true);
		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(false);

		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEinkommensverschlechterung(true);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(initEKVAppenzell());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		gesuch.getGesuchsteller2().setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setGemeinsameSteuererklaerung(true);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setGemeinsameSteuererklaerung(false);
		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(initEKVAppenzell());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(true);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
		gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(initEKVAppenzell());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));

		gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(initEKVAppenzell());
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG));
	}

	private Gesuch prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier mandantIdentifier, int anspruch) {
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(mandantIdentifier);
		Fall fall = new Fall();
		fall.setMandant(mandant);
		Dossier dossier = new Dossier();
		dossier.setFall(fall);
		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(dossier);
		gesuch.setStatus(AntragStatus.VERFUEGT);
		Betreuung betreuung = new Betreuung();
		betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		final Verfuegung verfuegungPreview = new Verfuegung();
		final VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.getRelevantBgCalculationResult().setAnspruchspensumProzent(anspruch);
		zeitabschnitt.getRelevantBgCalculationResult().setBetreuungspensumProzent(BigDecimal.valueOf(anspruch));
		verfuegungPreview.setZeitabschnitte(List.of(zeitabschnitt));
		betreuung.setVerfuegung(verfuegungPreview);
		KindContainer kindContainer = new KindContainer();
		kindContainer.setBetreuungen(betreuungen);
		gesuch.addKindContainer(kindContainer);
		return gesuch;
	}

	private FinSitZusatzangabenAppenzell initFinSitAppenzell() {
		FinSitZusatzangabenAppenzell finSitAR = new FinSitZusatzangabenAppenzell();
		finSitAR.setSaeule3a(BigDecimal.TEN);
		finSitAR.setSaeule3aNichtBvg(BigDecimal.TEN);
		finSitAR.setBeruflicheVorsorge(BigDecimal.TEN);
		finSitAR.setLiegenschaftsaufwand(BigDecimal.TEN);
		finSitAR.setEinkuenfteBgsa(BigDecimal.TEN);
		finSitAR.setVorjahresverluste(BigDecimal.TEN);
		finSitAR.setPolitischeParteiSpende(BigDecimal.TEN);
		finSitAR.setLeistungAnJuristischePersonen(BigDecimal.TEN);
		return finSitAR;
	}

	private Einkommensverschlechterung initEKVAppenzell() {
		Einkommensverschlechterung einkommensverschlechterung = new Einkommensverschlechterung();
		einkommensverschlechterung.setFinSitZusatzangabenAppenzell(initFinSitAppenzell());
		return einkommensverschlechterung;
	}

	private FinanzielleSituation initFinSitLuzern() {
		FinanzielleSituation finSitLuzern = new FinanzielleSituation();
		initAbstractFinSitLuzern(finSitLuzern);
		return finSitLuzern;
	}

	private Einkommensverschlechterung initEKVLuzern() {
		Einkommensverschlechterung einkommensverschlechterung = new Einkommensverschlechterung();
		initAbstractFinSitLuzern(einkommensverschlechterung);
		return einkommensverschlechterung;
	}

	private void initAbstractFinSitLuzern(AbstractFinanzielleSituation abstractFinanzielleSituation) {
		abstractFinanzielleSituation.setSteuerbaresEinkommen(BigDecimal.ONE);
		abstractFinanzielleSituation.setSteuerbaresVermoegen(BigDecimal.ONE);
		abstractFinanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ONE);
		abstractFinanzielleSituation.setGeschaeftsverlust(BigDecimal.ONE);
		abstractFinanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ONE);
	}

	private FinanzielleSituation initFinSitBern() {
		FinanzielleSituation finSit = new FinanzielleSituation();
		initAbstractFinSitBern(finSit);
		return finSit;
	}

	private Einkommensverschlechterung initEKVBern() {
		Einkommensverschlechterung einkommensverschlechterung = new Einkommensverschlechterung();
		initAbstractFinSitBern(einkommensverschlechterung);
		return einkommensverschlechterung;
	}

	private void initAbstractFinSitBern(AbstractFinanzielleSituation abstractFinanzielleSituation) {
		abstractFinanzielleSituation.setNettolohn(BigDecimal.ONE);
		abstractFinanzielleSituation.setFamilienzulage(BigDecimal.ONE);
		abstractFinanzielleSituation.setErsatzeinkommen(BigDecimal.ONE);
		abstractFinanzielleSituation.setErhalteneAlimente(BigDecimal.ONE);
		abstractFinanzielleSituation.setGeleisteteAlimente(BigDecimal.ONE);
		abstractFinanzielleSituation.setSchulden(BigDecimal.ONE);
		abstractFinanzielleSituation.setBruttovermoegen(BigDecimal.ONE);
	}
}
