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

package ch.dvbern.ebegu.tests.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.test.TestDataUtil.createDefaultInstitutionStammdaten;
import static ch.dvbern.ebegu.test.TestDataUtil.initVorgaengerVerfuegungenWithNULL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test der als Proof of Concept dienen soll fuer das Regelsystem
 * Testfall, welcher möglichst viele Regeln abhandeln soll:
 *
 * Gesuchsperiode            |----------------------------------------------------------|
 *
 * Erwerbspensum GS1  |------------------ 50 -----------------|
 * |---------------------- 30 ------------|
 * Erwerbspensum GS2         |-------------------- 90 + 10 ----------------------------|
 *
 * Wegzug                                                                       |---------------- - -
 *
 * Kind1                     |--------------- Kita 60, ---------------------------------|
 * |------------ Fachstelle 80 --------------|
 *
 * Kind2                     |-------------- Kita1 20 ----------------------------------|
 * |-------------- Kita2 40 ----------------------------------|
 *
 * Kind3                     |-------------- Schulamt 100 ------------------------------|
 *
 * Kind4                     |---------------- Kita 30 ---------------------------------|
 * |---- - - Kind 3 Monate alt - - -
 */
public class BetreuungsgutscheinEvaluatorTest extends AbstractBGRechnerTest {

	private static final Logger LOG = LoggerFactory.getLogger(BetreuungsgutscheinEvaluatorTest.class);

	private final DateRange erwerbspensumGS1_1 = new DateRange(LocalDate.of(2010, Month.FEBRUARY, 2), LocalDate.of(2017, Month.MARCH, 20));
	private final DateRange erwerbspensumGS1_2 = new DateRange(LocalDate.of(2017, Month.JANUARY, 1), LocalDate.of(2017, Month.JULY, 31));

	private final DateRange fachstelleGueltigkeit = new DateRange(LocalDate.of(2016, Month.AUGUST, 1), LocalDate.of(2017, Month.APRIL, 30));

	private final DateRange gesuchsperiode = new DateRange(LocalDate.of(2016, Month.AUGUST, 1), LocalDate.of(2017, Month.JULY, 31));

	private KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter = TestDataUtil.geKitaxUebergangsloesungParameter();

	@Test
	public void doTestEvaluation() {
		Gesuch testgesuch = createGesuch();
		testgesuch.setEingangsdatum(LocalDate.of(2016, 7, 1));
		testgesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(testgesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		for (KindContainer kindContainer : testgesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				assertNotNull(betreuung);
				LOG.info("{}", betreuung.getVerfuegungOrVerfuegungPreview());
			}
		}
	}

	@Test
	public void doTestEvaluationGeneratedBemerkungen() {
		Gesuch testgesuch = createGesuch();
		testgesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		testgesuch.getFinanzDatenDTO().setMassgebendesEinkBjVorAbzFamGr(new BigDecimal("500000")); //zu hoch -> Comment wird erzeugt

		evaluator.evaluate(testgesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);

		for (KindContainer kindContainer : testgesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
				assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview().getGeneratedBemerkungen());
				assertFalse(betreuung.getVerfuegungOrVerfuegungPreview().getGeneratedBemerkungen().isEmpty());
			}
		}
	}

	@Test
	public void doTestEvaluationForFamiliensituation() {
		Gesuch testgesuch = createGesuch();
		testgesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		Verfuegung verfuegung = evaluator.evaluateFamiliensituation(testgesuch, Constants.DEFAULT_LOCALE);

		assertNotNull(verfuegung);
		Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());

		Assert.assertEquals(MathUtil.EINE_NACHKOMMASTELLE.from(3.0d), verfuegung.getZeitabschnitte().get(0).getFamGroesse());
		Assert.assertEquals(0, new BigDecimal("20000").compareTo(verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommenVorAbzFamgr()));
		Assert.assertEquals(0, new BigDecimal("11400").compareTo(verfuegung.getZeitabschnitte().get(0).getAbzugFamGroesse()));
		Assert.assertEquals(0, new BigDecimal("8600").compareTo(verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen()));
	}

	@Test
	public void mutationMassgebendesEinkommenHalfedShouldBePositiveVeraenderung() {
		Gesuchsperiode gp = TestDataUtil.createGesuchsperiode1718();
		Gesuch testgesuch = createGesuch(gp);
		testgesuch.setGesuchsperiode(gp);
		testgesuch.setEingangsdatum(testgesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb().minusDays(1));

		evaluator.evaluate(testgesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		for (KindContainer kindContainer : testgesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				assertNotNull(betreuung);
				assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
			}
		}

		Gesuch mutation = setupMutationWithFractionOfEinkommenGS1(testgesuch, 2);

		evaluator.evaluate(mutation, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		for (KindContainer kindContainer : mutation.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				assertNotNull(betreuung);
				assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
				assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview().getVeraenderungVerguenstigungGegenueberVorgaenger());
				assertTrue(betreuung.getVerfuegungOrVerfuegungPreview().getVeraenderungVerguenstigungGegenueberVorgaenger().compareTo(BigDecimal.ZERO) > 0);
			}
		}
	}


	@Test
	public void mutationPositiveBGVeraenderungOnlyFinSitChangesShouldNotBeIgnorable() {
		Gesuchsperiode gp = TestDataUtil.createGesuchsperiode1718();
		Gesuch testgesuch = createGesuch(gp);
		testgesuch.setGesuchsperiode(gp);
		testgesuch.setEingangsdatum(testgesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb().minusDays(1));

		evaluator.evaluate(testgesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		for (KindContainer kindContainer : testgesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				assertNotNull(betreuung);
				assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
			}
		}

		Gesuch mutation = setupMutationWithFractionOfEinkommenGS1(testgesuch, 2);

		evaluator.evaluate(mutation, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		for (KindContainer kindContainer : mutation.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				assertNotNull(betreuung);
				assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
				assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview().getVeraenderungVerguenstigungGegenueberVorgaenger());
				assertTrue(betreuung.getVerfuegungOrVerfuegungPreview().getVeraenderungVerguenstigungGegenueberVorgaenger().compareTo(BigDecimal.ZERO) > 0);
				assertFalse(betreuung.getVerfuegungOrVerfuegungPreview().getIgnorable());
			}
		}
	}
	private Gesuch createGesuch() {
		Gesuch gesuch = new Gesuch();
		final Dossier dossier = TestDataUtil.createDefaultDossier();
		dossier.getFall().setFallNummer(2);
		gesuch.setDossier(dossier);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.initFamiliensituationContainer();
		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		assertNotNull(familiensituation);
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		// GS 1
		GesuchstellerContainer gsContainer1 = new GesuchstellerContainer();
		gsContainer1.setGesuchstellerJA(new Gesuchsteller());
		gesuch.setGesuchsteller1(gsContainer1);
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(20000));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(erwerbspensumGS1_1.getGueltigAb(), erwerbspensumGS1_1.getGueltigBis(), 50));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(erwerbspensumGS1_2.getGueltigAb(), erwerbspensumGS1_2.getGueltigBis(), 30));
		// GS 2
		GesuchstellerContainer gsContainer2 = new GesuchstellerContainer();
		gsContainer2.setGesuchstellerJA(new Gesuchsteller());
		gesuch.setGesuchsteller2(gsContainer2);
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		assertNotNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(gesuchsperiode.getGueltigAb(), gesuchsperiode.getGueltigBis(), 100));
		// Kind 1
		Betreuung betreuungKind1 = createBetreuungWithPensum(gesuch, gesuchsperiode);
		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setGueltigkeit(fachstelleGueltigkeit);
		pensumFachstelle.setPensum(80);
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		betreuungKind1.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);
		assertNotNull(betreuungKind1.getKind().getKindJA().getPensumFachstelle());
		betreuungKind1.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());

		initVorgaengerVerfuegungenWithNULL(gesuch);

		return gesuch;
	}

	private Gesuch createGesuch(Gesuchsperiode antragPeriode) {
		Gesuch gesuch = new Gesuch();
		final Dossier dossier = TestDataUtil.createDefaultDossier();
		dossier.getFall().setFallNummer(2);
		gesuch.setDossier(dossier);
		gesuch.setGesuchsperiode(antragPeriode);
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.initFamiliensituationContainer();
		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		assertNotNull(familiensituation);
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		// GS 1
		GesuchstellerContainer gsContainer1 = new GesuchstellerContainer();
		gsContainer1.setGesuchstellerJA(new Gesuchsteller());
		gsContainer1.addAdresse(TestDataUtil.createDefaultGesuchstellerAdresseContainer(gsContainer1));
		gesuch.setGesuchsteller1(gsContainer1);
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(100000));
		gesuch.getGesuchsteller1()
				.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
						LocalDate.of(
								antragPeriode.getGueltigkeit()
										.getGueltigAb()
										.getYear(),
								erwerbspensumGS1_1.getGueltigAb().getMonth(),
								erwerbspensumGS1_1.getGueltigAb().getDayOfMonth()),
						LocalDate.of(antragPeriode.getGueltigkeit().getGueltigBis().getYear(),
								erwerbspensumGS1_1.getGueltigBis().getMonth(),
								erwerbspensumGS1_1.getGueltigBis().getDayOfMonth()),
								50));
		gesuch.getGesuchsteller1()
				.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
						LocalDate.of(
								antragPeriode.getGueltigkeit()
										.getGueltigAb()
										.getYear(),
								erwerbspensumGS1_2.getGueltigAb().getMonth(),
								erwerbspensumGS1_2.getGueltigAb().getDayOfMonth()),
						LocalDate.of(antragPeriode.getGueltigkeit().getGueltigBis().getYear(),
								erwerbspensumGS1_2.getGueltigBis().getMonth(),
								erwerbspensumGS1_2.getGueltigBis().getDayOfMonth()),
								50));
		// GS 2
		GesuchstellerContainer gsContainer2 = new GesuchstellerContainer();
		gsContainer2.setGesuchstellerJA(new Gesuchsteller());
		gesuch.setGesuchsteller2(gsContainer2);
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		assertNotNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller2()
				.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
						antragPeriode.getGueltigkeit().getGueltigAb(),
						antragPeriode.getGueltigkeit().getGueltigBis(),
								100));
		// Kind 1
		Betreuung betreuungKind1 = createBetreuungWithPensum(gesuch, antragPeriode.getGueltigkeit());
		betreuungKind1.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());

		initVorgaengerVerfuegungenWithNULL(gesuch);

		return gesuch;
	}

	private Betreuung createBetreuungWithPensum(Gesuch gesuch, DateRange gueltigkeit) {
		Betreuung betreuung = new Betreuung();
		betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);

		KindContainer kindContainer = new KindContainer();
		betreuung.setKind(kindContainer);
		kindContainer.getBetreuungen().add(betreuung);
		betreuung.getKind().setKindJA(new Kind());
		betreuung.getKind().setGesuch(gesuch);
		betreuung.getKind().getKindJA().setGeburtsdatum(LocalDate.of(2017, 1, 7));
		betreuung.getKind().getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		betreuung.getKind().getKindJA().setKinderabzugZweitesHalbjahr(Kinderabzug.GANZER_ABZUG);
		gesuch.getKindContainers().add(betreuung.getKind());

		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten());
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);

		betreuung.setBetreuungspensumContainers(new HashSet<>());
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuung(betreuung);
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum());
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(gueltigkeit);
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(MathUtil.DEFAULT.from(60));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheBetreuungskosten(BigDecimal.valueOf(1000));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(BigDecimal.ZERO);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(BigDecimal.ZERO);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);

		ErweiterteBetreuungContainer erwBetContainer = TestDataUtil.createDefaultErweiterteBetreuungContainer();
		betreuung.setErweiterteBetreuungContainer(erwBetContainer);
		erwBetContainer.setBetreuung(betreuung);

		return betreuung;
	}

	private static Gesuch setupMutationWithFractionOfEinkommenGS1(Gesuch testgesuch, int fractionOfEinkommenGS1) {
		Gesuch mutation = testgesuch.copyForMutation(new Gesuch(), Eingangsart.ONLINE,
				Objects.requireNonNull(testgesuch.getEingangsdatum()));

		mutation.setGesuchsperiode(testgesuch.getGesuchsperiode());
		mutation.setEingangsdatum(mutation.getGesuchsperiode().getGueltigkeit().getGueltigAb().minusDays(1));
		assert mutation.getGesuchsteller1() != null;
		assert mutation.getGesuchsteller1().getFinanzielleSituationContainer() != null;
		assert mutation.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getNettolohn() != null;
		mutation.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setNettolohn(mutation.getGesuchsteller1()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA()
						.getNettolohn().divide(BigDecimal.valueOf(fractionOfEinkommenGS1)));

		TestDataUtil.calculateFinanzDaten(mutation, new FinanzielleSituationBernRechner());

		for (KindContainer kindContainer : mutation.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				Betreuung vorgaengerBetreuung = testgesuch.getKindContainers()
						.stream()
						.filter(kindContainer1 -> kindContainer1.getId().equals(kindContainer.getVorgaengerId()))
						.findFirst()
						.orElseThrow()
						.getBetreuungen()
						.stream()
						.filter(betreuung1 -> betreuung1.getId().equals(betreuung.getVorgaengerId()))
						.findFirst()
						.orElseThrow();
				betreuung.initVorgaengerVerfuegungen(vorgaengerBetreuung.getVerfuegungOrVerfuegungPreview(), new HashMap<>());
			}
		}
		return mutation;
	}
}
