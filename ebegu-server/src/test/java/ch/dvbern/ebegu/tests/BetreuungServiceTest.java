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
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.BelegungFerieninsel;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests fuer die Klasse betreuungService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class BetreuungServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private MitteilungService mitteilungService;
	@Inject
	private Persistence persistence;
	@Inject
	private KindService kindService;

	@Inject
	private InstitutionService institutionService;

	private Mandant mandant = null;
	private Benutzer empfaengerJA = null;
	private Benutzer sender = null;

	private Gesuchsperiode gesuchsperiode;

	@Before
	public void setUp() {
		gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
	}

	@Test
	public void createAndUpdateBetreuungTest() {
		assertNotNull(betreuungService);
		Betreuung persitedBetreuung = TestDataUtil.persistBetreuung(betreuungService, persistence, gesuchsperiode);
		Optional<Betreuung> betreuungOpt = betreuungService.findBetreuungWithBetreuungsPensen(persitedBetreuung.getId());
		assertTrue(betreuungOpt.isPresent());
		Betreuung betreuung = betreuungOpt.get();
		assertEquals(persitedBetreuung.getBetreuungsstatus(), betreuung.getBetreuungsstatus());

		assertEquals(GesuchBetreuungenStatus.WARTEN, betreuung.extractGesuch().getGesuchBetreuungenStatus());
		betreuung.setGrundAblehnung("abgewiesen");
		betreuung.setBetreuungsstatus(Betreuungsstatus.ABGEWIESEN);
		betreuungService.saveBetreuung(betreuung, false);
		Optional<Betreuung> updatedBetreuung = betreuungService.findBetreuung(persitedBetreuung.getId());
		assertTrue(updatedBetreuung.isPresent());

		assertEquals(GesuchBetreuungenStatus.ABGEWIESEN, updatedBetreuung.get().extractGesuch()
			.getGesuchBetreuungenStatus());
		assertEquals(Integer.valueOf(1), updatedBetreuung.get().getBetreuungNummer());
		final Optional<KindContainer> kind = kindService.findKind(betreuung.getKind().getId());
		assertTrue(kind.isPresent());
		assertEquals(Integer.valueOf(2), kind.get().getNextNumberBetreuung());
	}

	@Test
	public void removeBetreuungTest() {
		assertNotNull(betreuungService);
		Betreuung persitedBetreuung = TestDataUtil.persistBetreuung(betreuungService, persistence, gesuchsperiode);
		Optional<Betreuung> betreuungOptional = betreuungService.findBetreuung(persitedBetreuung.getId());
		assertTrue(betreuungOptional.isPresent());
		Betreuung betreuung = betreuungOptional.get();
		assertNotNull(betreuung.getErweiterteBetreuungContainer());

		final ErweiterteBetreuungContainer erweiterteBetreuungCont = persistence.find(ErweiterteBetreuungContainer.class, betreuung.getId());//shared id
		assertNotNull(erweiterteBetreuungCont);

		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer());
		persistence.merge(gesuch);

		final String gesuchId = betreuung.extractGesuch().getId();
		betreuungService.removeBetreuung(betreuung.getId());

		Optional<Betreuung> betreuungAfterRemove = betreuungService.findBetreuung(persitedBetreuung.getId());
		assertFalse(betreuungAfterRemove.isPresent());
		gesuch = persistence.find(Gesuch.class, gesuchId);
		assertEquals(GesuchBetreuungenStatus.ALLE_BESTAETIGT, gesuch.getGesuchBetreuungenStatus());
		final ErweiterteBetreuung erweiterteBetreuungAfterRemove = persistence.find(ErweiterteBetreuung.class, betreuung.getId());//shared id
		assertNull(erweiterteBetreuungAfterRemove);
	}



	@Test
	public void removeBetreuungWithMitteilungTest() {
		prepareDependentObjects();
		Gesuch dagmarGesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, LocalDate.now(), null, gesuchsperiode);
		Mitteilung mitteilung = TestDataUtil.createMitteilung(dagmarGesuch.getDossier(), empfaengerJA, MitteilungTeilnehmerTyp.JUGENDAMT,
			sender, MitteilungTeilnehmerTyp.GESUCHSTELLER);
		Betreuung betreuungUnderTest = dagmarGesuch.extractAllBetreuungen().get(0);
		mitteilung.setBetreuung(betreuungUnderTest);
		final Mitteilung persistedMitteilung = mitteilungService.sendMitteilung(mitteilung);
		assertEquals(betreuungUnderTest, persistedMitteilung.getBetreuung());

		Optional<Betreuung> betreuung = betreuungService.findBetreuung(betreuungUnderTest.getId());
		assertTrue(betreuung.isPresent());
		Collection<Mitteilung> mitteilungen = this.mitteilungService.findAllMitteilungenForBetreuung(betreuungUnderTest);
		assertEquals(1, mitteilungen.size());
		final Optional<Mitteilung> firstOpt = mitteilungen.stream().findFirst();
		assertTrue(firstOpt.isPresent());
		assertEquals(betreuungUnderTest, firstOpt.get().getBetreuung());
		betreuungService.removeBetreuung(betreuung.get().getId());
		Optional<Betreuung> betreuungAfterRemove = betreuungService.findBetreuung(betreuungUnderTest.getId());
		assertFalse(betreuungAfterRemove.isPresent());
		Collection<Mitteilung> mitteilungenAfterRemove = this.mitteilungService.findAllMitteilungenForBetreuung(betreuungUnderTest);
		assertEquals(0, mitteilungenAfterRemove.size());

		//die Mitteilung muss noch existieren
		Optional<Mitteilung> stillExistingMitteilung = this.mitteilungService.findMitteilung(firstOpt.get().getId());
		assertTrue(stillExistingMitteilung.isPresent());
		assertNotNull(stillExistingMitteilung.get());
		assertNull(stillExistingMitteilung.get().getBetreuung());

	}

	@Test
	public void removeBetreuungsmitteilungTest() throws LoginException {
		prepareDependentObjects();
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, LocalDate.now(), null, gesuchsperiode);
		final Betreuung betreuungUnderTest = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();

		loginAsSachbearbeiterInst("sainst", betreuungUnderTest.getInstitutionStammdaten().getInstitution());

		//create a first mitteilung
		final Betreuungsmitteilung betmitteilung = TestDataUtil.createBetreuungmitteilung(gesuch.getDossier(), empfaengerJA, MitteilungTeilnehmerTyp.JUGENDAMT,
			sender, MitteilungTeilnehmerTyp.INSTITUTION);
		betmitteilung.setBetreuung(betreuungUnderTest);
		final Betreuungsmitteilung persistedFirstMitteilung = mitteilungService.sendBetreuungsmitteilung(betmitteilung);

		loginAsSuperadmin();
		Optional<Betreuung> betreuung = betreuungService.findBetreuung(betreuungUnderTest.getId());
		assertTrue(betreuung.isPresent());
		Collection<Mitteilung> mitteilungen = this.mitteilungService.findAllMitteilungenForBetreuung(betreuungUnderTest);
		assertEquals(1, mitteilungen.size());
		assertEquals(betreuungUnderTest, mitteilungen.stream().findFirst().get().getBetreuung());
		assertEquals(persistedFirstMitteilung, mitteilungen.stream().findFirst().get());
		betreuungService.removeBetreuung(betreuung.get().getId());
		Optional<Betreuung> betreuungAfterRemove = betreuungService.findBetreuung(betreuungUnderTest.getId());
		assertFalse(betreuungAfterRemove.isPresent());
		Collection<Mitteilung> mitteilungenAfterRemove = this.mitteilungService.findAllMitteilungenForBetreuung(betreuungUnderTest);
		assertEquals(0, mitteilungenAfterRemove.size());
		//die Betreuungsmitteilung muss geloescht sein
		Optional<Mitteilung> removedMitteilung = this.mitteilungService.findMitteilung(mitteilungen.stream().findFirst().get().getId());
		assertFalse(removedMitteilung.isPresent());

	}

	@Test
	public void betreuungMitBelegungFerieninsel() {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, LocalDate.now(), null, gesuchsperiode);
		KindContainer kindContainer = gesuch.getKindContainers().iterator().next();
		TestDataUtil.saveInstitutionsstammdatenForTestfaelle(persistence, gesuchsperiode);
		kindContainer.getAnmeldungenFerieninsel().add(TestDataUtil.createAnmeldungFerieninsel(kindContainer));
		final AnmeldungFerieninsel betreuungUnderTest = kindContainer.getAnmeldungenFerieninsel().iterator().next();

		BelegungFerieninsel belegungFerieninsel = TestDataUtil.createDefaultBelegungFerieninsel();
		betreuungUnderTest.setBelegungFerieninsel(belegungFerieninsel);
		AnmeldungFerieninsel persistedBetreuung = betreuungService.saveAnmeldungFerieninsel(betreuungUnderTest);

		assertNotNull(persistedBetreuung);
		assertNotNull(persistedBetreuung.getBelegungFerieninsel());
		assertNotNull(persistedBetreuung.getBelegungFerieninsel().getFerienname());
		assertNotNull(persistedBetreuung.getBelegungFerieninsel().getTage());
		assertFalse(persistedBetreuung.getBelegungFerieninsel().getTage().isEmpty());
		assertEquals(1, persistedBetreuung.getBelegungFerieninsel().getTage().size());

		// Einen Tag hinzufügen
		persistedBetreuung.getBelegungFerieninsel().getTage().add(TestDataUtil.createBelegungFerieninselTag(LocalDate.now().plusMonths(4)));
		persistedBetreuung = betreuungService.saveAnmeldungFerieninsel(persistedBetreuung);
		assertNotNull(persistedBetreuung);
		assertNotNull(persistedBetreuung.getBelegungFerieninsel());
		assertNotNull(persistedBetreuung.getBelegungFerieninsel().getFerienname());
		assertNotNull(persistedBetreuung.getBelegungFerieninsel().getTage());
		assertFalse(persistedBetreuung.getBelegungFerieninsel().getTage().isEmpty());
		assertEquals(2, persistedBetreuung.getBelegungFerieninsel().getTage().size());

		// Einen wieder loeschen
		persistedBetreuung.getBelegungFerieninsel().getTage().remove(1);
		persistedBetreuung = betreuungService.saveAnmeldungFerieninsel(persistedBetreuung);
		assertNotNull(persistedBetreuung);
		assertNotNull(persistedBetreuung.getBelegungFerieninsel());
		assertNotNull(persistedBetreuung.getBelegungFerieninsel().getFerienname());
		assertNotNull(persistedBetreuung.getBelegungFerieninsel().getTage());
		assertFalse(persistedBetreuung.getBelegungFerieninsel().getTage().isEmpty());
		assertEquals(1, persistedBetreuung.getBelegungFerieninsel().getTage().size());
	}

	private void prepareDependentObjects() {
		mandant = TestDataUtil.createDefaultMandant();
		persistence.persist(mandant);
		empfaengerJA = TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SACHBEARBEITER_BG, "saja",
			null, null, mandant, persistence, null, null);
		persistence.persist(empfaengerJA);

		final Traegerschaft traegerschaft = persistence.persist(TestDataUtil.createDefaultTraegerschaft());
		Benutzer empfaengerINST = TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, "insti",
			traegerschaft, null, mandant, persistence, null, null);
		persistence.persist(empfaengerINST);

		sender = TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.GESUCHSTELLER, "gsst", null, null, mandant, persistence, null, null);
		persistence.persist(sender);
	}

	/**
	 * Kita-Zeitraum = Gesuchsperiode (mindestens)
	 */
	@Test
	public void validateBetreuungszeitraumInnerhalbInstitutionsGueltigkeit() {
		prepareDependentObjects();
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, LocalDate.now(), null, gesuchsperiode);
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();

		// *** Kita-Zeitraum = Gesuchsperiode (mindestens)
		LocalDate kitaFrom = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb();
		LocalDate kitaUntil = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis();
		InstitutionStammdaten institutionStammdaten = TestDataUtil.createDefaultInstitutionStammdaten();
		institutionStammdaten.getInstitution().setMandant(mandant);
		institutionStammdaten.getInstitution().setTraegerschaft(null);
		institutionStammdaten.getGueltigkeit().setGueltigAb(kitaFrom);
		institutionStammdaten.getGueltigkeit().setGueltigBis(kitaUntil);
		institutionStammdaten = TestDataUtil.saveInstitutionStammdatenIfNecessary(persistence, institutionStammdaten);
		betreuung.setInstitutionStammdaten(institutionStammdaten);

		// (1) Pensum exakt gleich wie Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom);
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil);
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (2) Pensum innerhalb Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.plusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil.minusDays(1));
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (3) Pensum ausserhalb Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.minusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil.plusDays(1));
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (4) Pensum innerhalb Kita-Zeitraum mit bis=END_OF_TIME
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.plusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (5) Pensum ausserhalb Kita-Zeitraum mit bis=END_OF_TIME
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.minusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);
	}

	/**
	 * Kita hat innerhalb GP neu geöffnet
	 */
	@Test
	public void validateBetreuungszeitraumInstitutionsGueltigkeitInGesuchsperiodeOpen() {
		prepareDependentObjects();
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, LocalDate.now(), null, gesuchsperiode);
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();

		// *** Kita hat innerhalb GP neu geöffnet
		LocalDate kitaFrom = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb().plusWeeks(1);
		LocalDate kitaUntil = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis();
		prepareInstitutionsstammdaten(betreuung, kitaFrom, kitaUntil);

		// (1) Pensum exakt gleich wie Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom);
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil);
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (2) Pensum innerhalb Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.plusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil.minusDays(1));
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (3) Pensum ausserhalb Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.minusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil.plusDays(1));
		try {
			betreuungService.betreuungPlatzBestaetigen(betreuung);
			fail("Exception expected");
		} catch (Exception e) {
			// Expected
		}

		// (4) Pensum innerhalb Kita-Zeitraum mit bis=END_OF_TIME
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.plusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (5) Pensum ausserhalb Kita-Zeitraum mit bis=END_OF_TIME
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.minusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		try {
			betreuungService.betreuungPlatzBestaetigen(betreuung);
			fail("Exception expected");
		} catch (Exception e) {
			// Expected
		}
	}

	/**
	 * Kita hat innerhalb GP geschlossen
	 */
	@Test
	public void validateBetreuungszeitraumInstitutionsGueltigkeitInGesuchsperiodeClosed() {
		prepareDependentObjects();
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, LocalDate.now(), null, gesuchsperiode);
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();

		// *** Kita wird innerhalb GP geschlossen
		LocalDate kitaFrom = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb();
		LocalDate kitaUntil = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis().minusWeeks(1);
		prepareInstitutionsstammdaten(betreuung, kitaFrom, kitaUntil);

		// (1) Pensum exakt gleich wie Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom);
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil);
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (2) Pensum innerhalb Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.plusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil.minusDays(1));
		betreuung = betreuungService.betreuungPlatzBestaetigen(betreuung);
		assertNotNull(betreuung);

		// (3) Pensum ausserhalb Kita-Zeitraum
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.minusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(kitaUntil.plusDays(1));
		try {
			betreuungService.betreuungPlatzBestaetigen(betreuung);
			fail("Exception expected");
		} catch (Exception e) {
			// Expected
		}

		// (4) Pensum innerhalb Kita-Zeitraum mit bis=END_OF_TIME
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.plusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		try {
			betreuungService.betreuungPlatzBestaetigen(betreuung);
			fail("Exception expected");
		} catch (Exception e) {
			// Expected
		}

		// (5) Pensum ausserhalb Kita-Zeitraum mit bis=END_OF_TIME
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigAb(kitaFrom.minusDays(1));
		betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA().getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		try {
			betreuungService.betreuungPlatzBestaetigen(betreuung);
			fail("Exception expected");
		} catch (Exception e) {
			// Expected
		}
	}

	@Test
	public void mapsId() {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, LocalDate.now(), null, gesuchsperiode);
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();

		Betreuung persistedBetreuung = betreuungService.saveBetreuung(betreuung, false);

		Assert.assertNotNull(persistedBetreuung);
		Assert.assertNotNull(persistedBetreuung.getErweiterteBetreuungContainer());

		ErweiterteBetreuungContainer ebc1 = persistence.find(
			ErweiterteBetreuungContainer.class, persistedBetreuung.getErweiterteBetreuungContainer().getId());
		ErweiterteBetreuungContainer ebc2 = persistence.find(
			ErweiterteBetreuungContainer.class, persistedBetreuung.getId());
		Assert.assertNotNull(ebc1);
		Assert.assertNotNull(ebc2);
		Assert.assertEquals(ebc1, ebc2);
	}

	private void prepareInstitutionsstammdaten(Betreuung betreuung, LocalDate kitaFrom, LocalDate kitaUntil) {
		InstitutionStammdaten institutionStammdaten = TestDataUtil.createDefaultInstitutionStammdaten();
		institutionStammdaten.getGueltigkeit().setGueltigAb(kitaFrom);
		institutionStammdaten.getGueltigkeit().setGueltigBis(kitaUntil);
		institutionStammdaten = TestDataUtil.saveInstitutionStammdatenIfNecessary(persistence, institutionStammdaten);
		betreuung.setInstitutionStammdaten(institutionStammdaten);
	}
}
