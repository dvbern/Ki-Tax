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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.TestfaelleService;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static ch.dvbern.ebegu.rechner.AbstractBGRechnerTest.checkTestfall01WaeltiDagmar;
import static java.util.Objects.requireNonNull;

/**
 * Tests fuer die Klasse VerfuegungService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class VerfuegungServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionService instService;

	@Inject
	private TestfaelleService testfaelleService;

	private Gesuchsperiode gesuchsperiode = new Gesuchsperiode();

	@Before
	public void setUp() {
		gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
	}

	@Test
	public void saveVerfuegung() {
		Assert.assertNotNull(verfuegungService); //init funktioniert
		Betreuung betreuung = insertBetreuung();
		Assert.assertNull(betreuung.getVerfuegungOrVerfuegungPreview());
		Verfuegung persistedVerfuegung = verfuegungService.verfuegen(
			betreuung.extractGesuch().getId(),
			betreuung.getId(),
			null,
			false,
			false,
			true);
		Betreuung persistedBetreuung = persistence.find(Betreuung.class, betreuung.getId());
		Assert.assertEquals(persistedVerfuegung.getBetreuung(), persistedBetreuung);
		Assert.assertEquals(persistedBetreuung.getVerfuegungOrVerfuegungPreview(), persistedVerfuegung);
		Assert.assertNotEquals("Die (nicht gespeicherte) Betreuung darf noch nicht verfügt sein", Betreuungsstatus.VERFUEGT, betreuung.getBetreuungsstatus());
		Assert.assertEquals("Die gespeicherte Betreuung muss jetzt verfügt sein", Betreuungsstatus.VERFUEGT, persistedBetreuung.getBetreuungsstatus());
	}

	@Test
	public void findVerfuegung() {
		Verfuegung verfuegung = insertVerfuegung();
		Optional<Verfuegung> loadedVerf = this.verfuegungService.findVerfuegung(verfuegung.getId());
		Assert.assertTrue(loadedVerf.isPresent());
		Assert.assertEquals(verfuegung, loadedVerf.get());
	}

	@Test
	public void calculateVerfuegung() {

		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.of(1980, Month.MARCH, 25),
			null,
			gesuchsperiode);
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertEquals(61, einstellungService.getAllEinstellungenBySystem(gesuch.getGesuchsperiode()).size());
		finanzielleSituationService.calculateFinanzDaten(gesuch);
		Gesuch berechnetesGesuch = this.verfuegungService.calculateVerfuegung(gesuch);
		Assert.assertNotNull(berechnetesGesuch);
		Assert.assertNotNull((berechnetesGesuch.getKindContainers().iterator().next()));
		Assert.assertNotNull((berechnetesGesuch.getKindContainers()
			.iterator()
			.next()
			.getBetreuungen()
			.iterator()
			.next()));
		Assert.assertNotNull(berechnetesGesuch.getKindContainers()
			.iterator()
			.next()
			.getBetreuungen()
			.iterator()
			.next()
			.getVerfuegungPreview());
		checkTestfall01WaeltiDagmar(gesuch);
	}

	@Test
	public void findVorgaengerVerfuegung() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.of(2016, Month.MARCH, 25),
			AntragStatus.VERFUEGT,
			gesuchsperiode);
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Set<KindContainer> kindContainers = gesuch.getKindContainers();
		KindContainer kind = kindContainers.iterator().next();
		Assert.assertEquals(1, kindContainers.size());
		Set<Betreuung> betreuungen = kind.getBetreuungen();
		Set<Betreuung> verfuegteBetr = betreuungen.stream()
			.map(this::createAndPersistVerfuegteVerfuegung)
			.collect(Collectors.toSet());
		kind.setBetreuungen(verfuegteBetr);
		Betreuung betreuung = verfuegteBetr.iterator().next();
		Integer betreuungNummer = betreuung.getBetreuungNummer();
		Verfuegung verfuegung1 = betreuung.getVerfuegungOrVerfuegungPreview();
		LocalDateTime timestampVerfuegt = LocalDateTime.of(2016, Month.APRIL, 1, 0, 0);
		gesuch.setTimestampVerfuegt(timestampVerfuegt);
		gesuch.setGueltig(true);
		Set<AntragStatusHistory> antragStatusHistories = gesuch.getAntragStatusHistories();
		AntragStatusHistory antragStatusHistory = new AntragStatusHistory();
		antragStatusHistory.setBenutzer(TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence));
		antragStatusHistory.setStatus(AntragStatus.VERFUEGT);
		antragStatusHistory.setGesuch(gesuch);
		antragStatusHistory.setTimestampVon(timestampVerfuegt);
		antragStatusHistories.add(antragStatusHistory);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getAdressen().get(0).setGesuchstellerContainer(gesuch.getGesuchsteller1());
		persistence.persist(gesuch.getGesuchsteller1().getAdressen().get(0));
		persistence.persist(antragStatusHistory);
		gesuch = persistence.merge(gesuch);

		Gesuch mutation = this.testfaelleService.antragMutieren(gesuch, LocalDate.now());
		Assert.assertNotNull(mutation);

		this.verfuegungService.initializeVorgaengerVerfuegungen(mutation);

		Optional<Betreuung> optFolgeBetreeung = mutation.getKindContainers().stream()
			.flatMap(k -> k.getBetreuungen().stream())
			.filter(b -> b.getBetreuungNummer().equals(betreuungNummer))
			.findAny();

		Assert.assertTrue(optFolgeBetreeung.isPresent());
		Assert.assertNotNull(optFolgeBetreeung.get().getVorgaengerVerfuegung());
		Assert.assertEquals(verfuegung1, optFolgeBetreeung.get().getVorgaengerVerfuegung());
	}

	@Test
	public void getAll() {
		Verfuegung verfuegung = insertVerfuegung();
		Assert.assertNotNull(verfuegung.getBetreuung());
		Assert.assertEquals(Betreuungsstatus.VERFUEGT, verfuegung.getBetreuung().getBetreuungsstatus());

		Betreuung betreuung = persistence.find(Betreuung.class, verfuegung.getBetreuung().getId());
		Assert.assertNotNull(betreuung);
		Assert.assertEquals(Betreuungsstatus.VERFUEGT, betreuung.getBetreuungsstatus());

		Verfuegung verfuegung2 = insertVerfuegung();
		Collection<Verfuegung> allVerfuegungen = this.verfuegungService.getAllVerfuegungen();
		Assert.assertEquals(2, allVerfuegungen.size());
		Assert.assertTrue(allVerfuegungen.stream()
			.allMatch(currentVerfuegung -> currentVerfuegung.equals(verfuegung)
				|| currentVerfuegung.equals(verfuegung2)));
	}

	@Test
	public void findVerrechnetenVorgaengerNoVorgaenger() {
		VerfuegungZeitabschnitt zeitabschnitt = createGesuchWithVerfuegungZeitabschnitt();
		Assert.assertNotNull(zeitabschnitt.getVerfuegung().getBetreuung());

		List<VerfuegungZeitabschnitt> zeitabschnittListe = new ArrayList<>();
		verfuegungService.findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(
			ZahlungslaufTyp.GEMEINDE_INSTITUTION,
			zeitabschnitt,
			zeitabschnitt.getVerfuegung().getBetreuung(),
			zeitabschnittListe);

		Assert.assertEquals(0, zeitabschnittListe.size());
	}

	//Helpers

	private Betreuung insertBetreuung() {
		Betreuung betreuung = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.of(1980, Month.MARCH, 25),
			null,
			gesuchsperiode
		).getKindContainers().iterator().next().getBetreuungen().iterator().next();
		return persistence.merge(betreuung);
	}

	private Verfuegung insertVerfuegung() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.of(1980, Month.MARCH, 25),
			null,
			gesuchsperiode);
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		return requireNonNull(createAndPersistVerfuegteVerfuegung(betreuung).getVerfuegung());
	}

	@Nonnull
	private Betreuung createAndPersistVerfuegteVerfuegung(@Nonnull Betreuung betreuung) {
		final Verfuegung createdVerf =
		verfuegungService.verfuegen(
			betreuung.extractGesuch().getId(),
			betreuung.getId(),
			null,
			false,
			false,
			true);
		Assert.assertNotNull(createdVerf.getBetreuung());
		return createdVerf.getBetreuung();
	}

	private VerfuegungZeitabschnitt createGesuchWithVerfuegungZeitabschnitt() {
		Verfuegung verfuegung = insertVerfuegung();
		VerfuegungZeitabschnitt zeitabschnitt = TestDataUtil.createDefaultZeitabschnitt(verfuegung);
		zeitabschnitt.initBGCalculationResult();
		persistence.persist(zeitabschnitt);
		return zeitabschnitt;
	}
}

