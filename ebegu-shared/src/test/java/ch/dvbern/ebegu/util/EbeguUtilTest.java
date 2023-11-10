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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
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
import org.junit.Assert;
import org.junit.Test;

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
		// 0 Anspruch aber beim Appenzell ist einer Ausnahme so es muss immer sein:
		Assert.assertTrue(EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch));
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
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationRequired(gesuch));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setSozialhilfeBezueger(false);
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setVerguenstigungGewuenscht(false);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationRequired(gesuch));

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setVerguenstigungGewuenscht(true);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationRequired(gesuch));
	}

	@Test
	public void isFinanzielleSituationIntroducedAndComplete_SOLOTHURN_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.SOLOTHURN);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		gesuch.setGesuchsteller1(gesuchstellerContainer);
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
	public void isFinanzielleSituationIntroducedAndComplete_LUZERN_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

	}

	@Test
	public void isFinanzielleSituationIntroducedAndComplete_AR_Test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.APPENZELL);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(new Familiensituation());
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		gesuch.setGesuchsteller1(gesuchstellerContainer);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		FinanzielleSituation finSit = new FinanzielleSituation();
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(finSit);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		FinSitZusatzangabenAppenzell finSitAR = new FinSitZusatzangabenAppenzell();
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setFinSitZusatzangabenAppenzell(finSitAR);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));


		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setSaeule3a(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setSaeule3aNichtBvg(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setBeruflicheVorsorge(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setLiegenschaftsaufwand(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setEinkuenfteBgsa(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setVorjahresverluste(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setPolitischeParteiSpende(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setLeistungAnJuristischePersonen(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setSteuerbaresVermoegen(BigDecimal.TEN);
		Assert.assertFalse(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().setSteuerbaresEinkommen(BigDecimal.TEN);
		Assert.assertTrue(EbeguUtil.isFinanzielleSituationIntroducedAndComplete(gesuch, WizardStepName.FINANZIELLE_SITUATION));
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
}
