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

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.WizardStepService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test fuer WizardStep Service
 */
@SuppressWarnings("InstanceMethodNamingConvention")
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class WizardStepServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private Persistence persistence;
	@Inject
	private InstitutionService instService;

	private Gesuch gesuch;
	private WizardStep betreuungStep;
	private WizardStep kinderStep;
	private WizardStep erwerbStep;
	private WizardStep familienStep;
	private WizardStep gesuchstellerStep;
	private WizardStep finanSitStep;
	private WizardStep einkVerStep;
	private WizardStep dokStep;
	private WizardStep freigabeStep;
	private WizardStep verfStep;

	@Before
	public void setUp() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
		gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence, LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);
		gesuch.setVorgaengerId(gesuch.getId()); // by default and to simplify itself
		gesuch = persistence.merge(gesuch);

		wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.GESUCH_ERSTELLEN, WizardStepStatus.OK));
		familienStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.FAMILIENSITUATION, WizardStepStatus.UNBESUCHT));
		gesuchstellerStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.GESUCHSTELLER, WizardStepStatus.UNBESUCHT));
		kinderStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.KINDER, WizardStepStatus.UNBESUCHT));
		betreuungStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.BETREUUNG, WizardStepStatus.UNBESUCHT));
		erwerbStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.ERWERBSPENSUM, WizardStepStatus.UNBESUCHT));
		finanSitStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.FINANZIELLE_SITUATION, WizardStepStatus.UNBESUCHT));
		einkVerStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.EINKOMMENSVERSCHLECHTERUNG, WizardStepStatus.UNBESUCHT));
		dokStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.DOKUMENTE, WizardStepStatus.UNBESUCHT));
		freigabeStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.FREIGABE, WizardStepStatus.UNBESUCHT));
		verfStep = wizardStepService.saveWizardStep(TestDataUtil.createWizardStepObject(gesuch, WizardStepName.VERFUEGEN, WizardStepStatus.UNBESUCHT));
	}

	@Test
	public void createWizardStepListForGesuchTest() {
		final Gesuch myGesuch = TestDataUtil.createAndPersistGesuch(persistence);
		TestDataUtil.prepareParameters(myGesuch.getGesuchsperiode(), persistence);
		final List<WizardStep> wizardStepList = wizardStepService.createWizardStepList(myGesuch);
		Assert.assertNotNull(wizardStepList);
		Assert.assertEquals(13, wizardStepList.size());

		wizardStepList.forEach(wizardStep -> {
			if (WizardStepName.GESUCH_ERSTELLEN == wizardStep.getWizardStepName()) {
				Assert.assertTrue(wizardStep.getVerfuegbar());
				Assert.assertEquals(WizardStepStatus.OK, wizardStep.getWizardStepStatus());
			} else {
				Assert.assertFalse(wizardStep.getVerfuegbar());
				Assert.assertEquals(WizardStepStatus.UNBESUCHT, wizardStep.getWizardStepStatus());
			}
		});
	}

	@Test
	public void createWizardStepListForLuzernTest() {
		final Gesuch myGesuch = TestDataUtil.createAndPersistGesuch(persistence);
		TestDataUtil.prepareParameters(myGesuch.getGesuchsperiode(), persistence);
		myGesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		final List<WizardStep> wizardStepList = wizardStepService.createWizardStepList(myGesuch);
		Assert.assertNotNull(wizardStepList);
		Assert.assertEquals(13, wizardStepList.size());

		var finSitLuzernFound = false;
		for (var wizardStep : wizardStepList) {
			Assert.assertNotEquals(WizardStepName.FINANZIELLE_SITUATION, wizardStep.getWizardStepName());
			if (wizardStep.getWizardStepName() == WizardStepName.FINANZIELLE_SITUATION_LUZERN) {
				finSitLuzernFound = true;
				Assert.assertFalse(wizardStep.getVerfuegbar());
				Assert.assertEquals(WizardStepStatus.UNBESUCHT, wizardStep.getWizardStepStatus());
			}
		}
		Assert.assertTrue(finSitLuzernFound);
	}

	@Test
	public void createWizardStepListForSolothurnTest() {
		final Gesuch myGesuch = TestDataUtil.createAndPersistGesuch(persistence);
		TestDataUtil.prepareParameters(myGesuch.getGesuchsperiode(), persistence);
		myGesuch.setFinSitTyp(FinanzielleSituationTyp.SOLOTHURN);
		final List<WizardStep> wizardStepList = wizardStepService.createWizardStepList(myGesuch);
		Assert.assertNotNull(wizardStepList);
		Assert.assertEquals(13, wizardStepList.size());

		var finSitSolothurnFound = false;
		for (var wizardStep : wizardStepList) {
			Assert.assertNotEquals(WizardStepName.FINANZIELLE_SITUATION, wizardStep.getWizardStepName());
			if (wizardStep.getWizardStepName() == WizardStepName.FINANZIELLE_SITUATION_SOLOTHURN) {
				finSitSolothurnFound = true;
				Assert.assertFalse(wizardStep.getVerfuegbar());
				Assert.assertEquals(WizardStepStatus.UNBESUCHT, wizardStep.getWizardStepStatus());
			}
		}
		Assert.assertTrue(finSitSolothurnFound);
	}

	@Test
	public void updateWizardStepGesuchErstellen() {
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.GESUCH_ERSTELLEN);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.GESUCH_ERSTELLEN).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepFamiliensituation() {
		updateStatus(familienStep, WizardStepStatus.IN_BEARBEITUNG);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.FAMILIENSITUATION);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.FAMILIENSITUATION).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepFamiliensituationWhenItDoesntExistYet() {
		updateStatus(familienStep, WizardStepStatus.IN_BEARBEITUNG);
		updateStatus(gesuchstellerStep, WizardStepStatus.IN_BEARBEITUNG);

		//oldData ist eine leere Familiensituation
		final Familiensituation newFamiliensituation = TestDataUtil.createDefaultFamiliensituation();
		final Familiensituation vorgaenger = TestDataUtil.createDefaultFamiliensituation();
		newFamiliensituation.setVorgaengerId(vorgaenger.getId());
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(),
			new Familiensituation(), newFamiliensituation, WizardStepName.FAMILIENSITUATION);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.FAMILIENSITUATION).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepFamiliensituationFromOneToTwoGS() {
		updateStatus(familienStep, WizardStepStatus.IN_BEARBEITUNG);
		updateStatus(gesuchstellerStep, WizardStepStatus.OK);
		updateStatus(finanSitStep, WizardStepStatus.OK);
		updateStatus(einkVerStep, WizardStepStatus.OK);

		FamiliensituationContainer familiensituationContainer = gesuch.getFamiliensituationContainer();
		Assert.assertNotNull(familiensituationContainer);
		assert familiensituationContainer.getFamiliensituationJA() != null;
		Familiensituation oldFamiliensituation = new Familiensituation(familiensituationContainer.getFamiliensituationJA());
		final Familiensituation newFamiliensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(newFamiliensituation);
		newFamiliensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		final Familiensituation vorgaenger = TestDataUtil.createDefaultFamiliensituation();
		newFamiliensituation.setVorgaengerId(vorgaenger.getId());

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), oldFamiliensituation,
			newFamiliensituation, WizardStepName.FAMILIENSITUATION);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.FAMILIENSITUATION).getWizardStepStatus());
		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.GESUCHSTELLER).getWizardStepStatus());
		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.FINANZIELLE_SITUATION).getWizardStepStatus());
		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.EINKOMMENSVERSCHLECHTERUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepGesuchstellerNichtFuerUNBESUCHT() {
		updateStatus(gesuchstellerStep, WizardStepStatus.IN_BEARBEITUNG);
		updateStatusVerfuegbar(finanSitStep, WizardStepStatus.UNBESUCHT);
		updateStatusVerfuegbar(einkVerStep, WizardStepStatus.UNBESUCHT);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.GESUCHSTELLER);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.GESUCH_ERSTELLEN).getWizardStepStatus());
		Assert.assertFalse(findStepByName(wizardSteps, WizardStepName.FINANZIELLE_SITUATION).getVerfuegbar());
		Assert.assertFalse(findStepByName(wizardSteps, WizardStepName.EINKOMMENSVERSCHLECHTERUNG).getVerfuegbar());
	}

	@Test
	public void updateWizardStepGesuchsteller() {
		updateStatus(gesuchstellerStep, WizardStepStatus.IN_BEARBEITUNG);
		updateStatusVerfuegbar(finanSitStep, WizardStepStatus.IN_BEARBEITUNG);
		updateStatusVerfuegbar(einkVerStep, WizardStepStatus.IN_BEARBEITUNG);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.GESUCHSTELLER);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.GESUCH_ERSTELLEN).getWizardStepStatus());
		Assert.assertTrue(findStepByName(wizardSteps, WizardStepName.FINANZIELLE_SITUATION).getVerfuegbar());
		Assert.assertTrue(findStepByName(wizardSteps, WizardStepName.EINKOMMENSVERSCHLECHTERUNG).getVerfuegbar());
	}

	@Test
	public void updateWizardStepKinder() {
		updateStatus(kinderStep, WizardStepStatus.IN_BEARBEITUNG);
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.KINDER);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.KINDER).getWizardStepStatus());
		Assert.assertEquals(WizardStepStatus.PLATZBESTAETIGUNG, findStepByName(wizardSteps, WizardStepName.BETREUUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepKinderNOKIfNoKinder() {
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);
		updateStatus(kinderStep, WizardStepStatus.IN_BEARBEITUNG);

		final Iterator<KindContainer> kinderIterator = gesuch.getKindContainers().iterator();
		KindContainer kind = kinderIterator.next();

		final Iterator<Betreuung> betreuungIterator = kind.getBetreuungen().iterator();
		betreuungIterator.forEachRemaining(betreuung -> {
			persistence.remove(ErweiterteBetreuungContainer.class, betreuung.getErweiterteBetreuungContainer().getId());
			persistence.remove(Betreuung.class, betreuung.getId());
		});

		persistence.remove(KindContainer.class, kind.getId());

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.KINDER);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.KINDER).getWizardStepStatus());
		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.BETREUUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepKinderNOKIfKindNoBedarf() {
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);
		updateStatus(kinderStep, WizardStepStatus.IN_BEARBEITUNG);

		final Iterator<KindContainer> kinderIterator = gesuch.getKindContainers().iterator();
		KindContainer kind = kinderIterator.next();
		kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
		persistence.merge(kind);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.KINDER);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.KINDER).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepBetreuungUnbesucht() {
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.BETREUUNG);
		Assert.assertEquals(11, wizardSteps.size());
		// shouldn't update it because the current status is unbesucht
		Assert.assertEquals(WizardStepStatus.UNBESUCHT, findStepByName(wizardSteps, WizardStepName.BETREUUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepBetreuungNOKIfBetreuungAbgewiesen() {
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);

		final Iterator<Betreuung> betreuungIterator = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator();
		Betreuung betreuung = betreuungIterator.next();
		betreuung = persistence.find(Betreuung.class, betreuung.getId());
		betreuung.setBetreuungsstatus(Betreuungsstatus.ABGEWIESEN);
		betreuung.setGrundAblehnung("Abgelehnt");
		persistence.merge(betreuung);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.BETREUUNG);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.BETREUUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepBetreuungPlatzBestaetigung() {
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);

		final Iterator<Betreuung> betreuungIterator = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator();
		Betreuung betreuung = betreuungIterator.next();
		betreuung = persistence.find(Betreuung.class, betreuung.getId());
		betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
		persistence.merge(betreuung);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.BETREUUNG);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.PLATZBESTAETIGUNG, findStepByName(wizardSteps, WizardStepName.BETREUUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepBetreuungNOKIfNoBetreuung() {
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);

		final Iterator<Betreuung> betreuungIterator = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator();
		Betreuung betreuung1 = betreuungIterator.next();
		persistence.remove(Betreuung.class, betreuung1.getId());
		Betreuung betreuung2 = betreuungIterator.next();
		persistence.remove(Betreuung.class, betreuung2.getId());

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.BETREUUNG);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.BETREUUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepBetreuungOK() {
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.BETREUUNG);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.PLATZBESTAETIGUNG, findStepByName(wizardSteps, WizardStepName.BETREUUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepErwerbspensumOKNotRequired() {
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);

		final Iterator<Betreuung> betreuungIterator = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator();
		InstitutionStammdaten institutionStammdaten1 = betreuungIterator.next().getInstitutionStammdaten();
		institutionStammdaten1 = persistence.find(InstitutionStammdaten.class, institutionStammdaten1.getId());
		institutionStammdaten1.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		persistence.merge(institutionStammdaten1);

		InstitutionStammdaten institutionStammdaten2 = betreuungIterator.next().getInstitutionStammdaten();
		institutionStammdaten2 = persistence.find(InstitutionStammdaten.class, institutionStammdaten2.getId());
		institutionStammdaten2.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		persistence.merge(institutionStammdaten2);

		GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		Assert.assertNotNull(gesuchsteller1);
		ErwerbspensumContainer erwerbspensumContainer = gesuchsteller1.getErwerbspensenContainers().iterator().next();
		persistence.remove(ErwerbspensumContainer.class, erwerbspensumContainer.getId());

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.ERWERBSPENSUM);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.ERWERBSPENSUM).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepErwerbspensumNOKWhenRequired() {
		updateStatus(betreuungStep, WizardStepStatus.IN_BEARBEITUNG);

		GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		Assert.assertNotNull(gesuchsteller1);
		ErwerbspensumContainer erwerbspensumContainer = gesuchsteller1.getErwerbspensenContainers().iterator().next();
		persistence.remove(ErwerbspensumContainer.class, erwerbspensumContainer.getId());

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.ERWERBSPENSUM);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.ERWERBSPENSUM).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepErwerbspensum() {
		updateStatus(erwerbStep, WizardStepStatus.IN_BEARBEITUNG);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.ERWERBSPENSUM);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.ERWERBSPENSUM).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepEinkommensverschlechterungTrueToFalse() {
		updateWizardStepEinkommensverschlechterung(true);
	}

	@Test
	public void updateWizardStepEinkommensverschlechterungFalseToFalse() {
		updateWizardStepEinkommensverschlechterung(false);
	}

	private void updateWizardStepEinkommensverschlechterung(final boolean oldValue) {
		updateStatus(einkVerStep, WizardStepStatus.IN_BEARBEITUNG);
		EinkommensverschlechterungInfo oldData = new EinkommensverschlechterungInfo();
		oldData.setEinkommensverschlechterung(oldValue);
		EinkommensverschlechterungInfo newData = new EinkommensverschlechterungInfo();
		newData.setEinkommensverschlechterung(false);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), oldData,
			newData, WizardStepName.EINKOMMENSVERSCHLECHTERUNG);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.EINKOMMENSVERSCHLECHTERUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepEinkommensverschlechterungNOK() {
		updateStatus(einkVerStep, WizardStepStatus.IN_BEARBEITUNG);
		EinkommensverschlechterungInfoContainer oldDataCont = new EinkommensverschlechterungInfoContainer();
		EinkommensverschlechterungInfo oldData = new EinkommensverschlechterungInfo();
		oldData.setEinkommensverschlechterung(false);
		oldDataCont.setEinkommensverschlechterungInfoJA(oldData);

		EinkommensverschlechterungInfoContainer newDataCont = new EinkommensverschlechterungInfoContainer();
		EinkommensverschlechterungInfo newData = new EinkommensverschlechterungInfo();
		newData.setEinkommensverschlechterung(true);
		newDataCont.setEinkommensverschlechterungInfoJA(newData);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), oldDataCont,
			newDataCont, WizardStepName.EINKOMMENSVERSCHLECHTERUNG);
		Assert.assertEquals(11, wizardSteps.size());

		//status is NOK weil die Daten noch nicht eingetragen sind
		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.EINKOMMENSVERSCHLECHTERUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepEinkommensverschlechterungNOKNull() {
		updateStatus(einkVerStep, WizardStepStatus.IN_BEARBEITUNG);
		EinkommensverschlechterungInfoContainer newDataCont = new EinkommensverschlechterungInfoContainer();
		EinkommensverschlechterungInfo newData = new EinkommensverschlechterungInfo();
		newData.setEinkommensverschlechterung(true);
		newDataCont.setEinkommensverschlechterungInfoJA(newData);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null,
			newDataCont, WizardStepName.EINKOMMENSVERSCHLECHTERUNG);
		Assert.assertEquals(11, wizardSteps.size());

		//status is NOK weil die Daten noch nicht eingetragen sind
		Assert.assertEquals(WizardStepStatus.NOK, findStepByName(wizardSteps, WizardStepName.EINKOMMENSVERSCHLECHTERUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepDokumente() {
		updateStatus(dokStep, WizardStepStatus.IN_BEARBEITUNG);

		createAndPersistDokumentGrundWithDokument(DokumentGrundTyp.FINANZIELLESITUATION, DokumentTyp.STEUERVERANLAGUNG, "2016");
		createAndPersistDokumentGrundWithDokument(DokumentGrundTyp.ERWERBSPENSUM, DokumentTyp.NACHWEIS_ERWERBSPENSUM, "Angestellt 60%");

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.DOKUMENTE);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.IN_BEARBEITUNG, findStepByName(wizardSteps, WizardStepName.DOKUMENTE).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepDokumenteIN_BEARBEITUNG() {
		updateStatus(dokStep, WizardStepStatus.IN_BEARBEITUNG);

		//nicht alle notwendige dokumente
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.DOKUMENTE);
		Assert.assertEquals(11, wizardSteps.size());
		Assert.assertEquals(WizardStepStatus.IN_BEARBEITUNG, findStepByName(wizardSteps, WizardStepName.DOKUMENTE).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepFamiliensitMutiertSameData() {
		updateStatusMutiert(familienStep, WizardStepStatus.OK);
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		familiensituation.setVorgaengerId(familiensituation.getId()); // same data
		persistence.merge(familiensituation);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), familiensituation,
			familiensituation, WizardStepName.FAMILIENSITUATION);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.OK, findStepByName(wizardSteps, WizardStepName.FAMILIENSITUATION).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepFamiliensitMutiert() {
		updateStatusMutiert(familienStep, WizardStepStatus.OK);
		final Familiensituation vorgaenger = TestDataUtil.createDefaultFamiliensituation();
		vorgaenger.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		familiensituation.setVorgaengerId(vorgaenger.getId());
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), familiensituation,
			familiensituation, WizardStepName.FAMILIENSITUATION);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.MUTIERT, findStepByName(wizardSteps, WizardStepName.FAMILIENSITUATION).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepGesuchstellerMutiert() {
		updateStatusMutiert(gesuchstellerStep, WizardStepStatus.OK);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null,
			null, WizardStepName.GESUCHSTELLER);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.MUTIERT, findStepByName(wizardSteps, WizardStepName.GESUCHSTELLER).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepKinderMutiert() {
		updateStatusMutiert(kinderStep, WizardStepStatus.OK);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null,
			null, WizardStepName.KINDER);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.MUTIERT, findStepByName(wizardSteps, WizardStepName.KINDER).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepErwerbspensumMutiert() {
		updateStatusMutiert(erwerbStep, WizardStepStatus.OK);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null,
			null, WizardStepName.ERWERBSPENSUM);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.MUTIERT, findStepByName(wizardSteps, WizardStepName.ERWERBSPENSUM).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepFinsintMutiert() {
		updateStatusMutiert(finanSitStep, WizardStepStatus.OK);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null,
			null, WizardStepName.FINANZIELLE_SITUATION);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.MUTIERT, findStepByName(wizardSteps, WizardStepName.FINANZIELLE_SITUATION).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepEkvMutiert() {
		Gesuch erstgesuch = gesuch;

		updateStatusMutiert(einkVerStep, WizardStepStatus.OK);

		EinkommensverschlechterungInfoContainer oldDataCont = new EinkommensverschlechterungInfoContainer();
		EinkommensverschlechterungInfo oldData = new EinkommensverschlechterungInfo();
		oldData.setEinkommensverschlechterung(true);
		oldData.setEkvFuerBasisJahrPlus1(true); // actual difference
		oldData.setEkvFuerBasisJahrPlus2(false);
		oldDataCont.setGesuch(gesuch);
		oldDataCont.setEinkommensverschlechterungInfoJA(oldData);

		gesuch.setEinkommensverschlechterungInfoContainer(oldDataCont);
		persistence.merge(oldData);

		EinkommensverschlechterungInfoContainer newDataCont = new EinkommensverschlechterungInfoContainer();
		EinkommensverschlechterungInfo newData = new EinkommensverschlechterungInfo();
		newData.setEinkommensverschlechterung(false);
		newData.setEkvFuerBasisJahrPlus1(false);
		newData.setEkvFuerBasisJahrPlus2(false);
		gesuch = persistence.find(Gesuch.class, gesuch.getId()); //reload gesuch to avoid transaction problems
		newDataCont.setGesuch(gesuch);
		newDataCont.setEinkommensverschlechterungInfoJA(newData);
		newDataCont.setVorgaengerId(oldDataCont.getId());

		gesuch.setEinkommensverschlechterungInfoContainer(newDataCont);
		gesuch.setVorgaengerId(erstgesuch.getId());
		newDataCont = persistence.persist(newDataCont);
		gesuch = persistence.merge(gesuch);

		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(),
			oldDataCont, newDataCont, WizardStepName.EINKOMMENSVERSCHLECHTERUNG);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.MUTIERT, findStepByName(wizardSteps, WizardStepName.EINKOMMENSVERSCHLECHTERUNG).getWizardStepStatus());
	}

	@Test
	public void updateWizardStepDokumenteMutiert() {
		updateStatusMutiert(dokStep, WizardStepStatus.OK);
		final List<WizardStep> wizardSteps = wizardStepService.updateSteps(gesuch.getId(), null,
			null, WizardStepName.DOKUMENTE);
		Assert.assertEquals(11, wizardSteps.size());

		Assert.assertEquals(WizardStepStatus.MUTIERT, findStepByName(wizardSteps, WizardStepName.DOKUMENTE).getWizardStepStatus());
	}

	@Test
	public void findWizardStepFromGesuch() {
		final WizardStep wizardStepFromGesuch = wizardStepService.findWizardStepFromGesuch(gesuch.getId(), WizardStepName.GESUCH_ERSTELLEN);
		Assert.assertEquals(WizardStepName.GESUCH_ERSTELLEN, wizardStepFromGesuch.getWizardStepName());
	}

	@Test
	public void findWizardStepFromGesuchNonExisting() {
		persistence.remove(WizardStep.class, freigabeStep.getId());
		final WizardStep wizardStepFromGesuch = wizardStepService.findWizardStepFromGesuch(gesuch.getId(), WizardStepName.FREIGABE);
		Assert.assertNull(wizardStepFromGesuch);
	}

	// HELP METHODS

	private void createAndPersistDokumentGrundWithDokument(DokumentGrundTyp dokGrundTyp, DokumentTyp dokTyp, String tag) {
		DokumentGrund dokGrund = new DokumentGrund();
		dokGrund.setDokumentGrundTyp(dokGrundTyp);
		dokGrund.setDokumentTyp(dokTyp);
		dokGrund.setNeeded(true);
		dokGrund.setGesuch(gesuch);
		GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		Assert.assertNotNull(gesuchsteller1);
		dokGrund.setTag(tag);
		persistence.persist(dokGrund);
		Dokument dok1 = new Dokument();
		dok1.setFilename("name");
		dok1.setFilepfad("pfad");
		dok1.setFilesize("23");
		dok1.setTimestampUpload(LocalDateTime.now());
		dok1.setDokumentGrund(dokGrund);
		persistence.persist(dok1);
	}

	@Nonnull
	private WizardStep findStepByName(List<WizardStep> wizardSteps, WizardStepName stepName) {
		for (WizardStep wizardStep : wizardSteps) {
			if (wizardStep.getWizardStepName() == stepName) {
				return wizardStep;
			}
		}
		Assert.fail("Step nicht gefunden!");
		throw new RuntimeException("Step nicht gefunden");
	}

	private void updateStatus(WizardStep step, WizardStepStatus status) {
		step.setWizardStepStatus(status);
		wizardStepService.saveWizardStep(step);
	}

	private void updateStatusMutiert(WizardStep step, WizardStepStatus status) {
		step.getGesuch().setTyp(AntragTyp.MUTATION);
		persistence.merge(step.getGesuch());
		step.setWizardStepStatus(status);
		wizardStepService.saveWizardStep(step);
	}

	private void updateStatusVerfuegbar(WizardStep step, WizardStepStatus status) {
		step.setVerfuegbar(false);
		step.setWizardStepStatus(status);
		wizardStepService.saveWizardStep(step);
	}

}
