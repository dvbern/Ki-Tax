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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Arquillian Tests fuer die Klasse FallService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)  //disabeln sonst existiert in changeVerantwortlicherOfFallTest der Benutzer noch gar nicht
public class FallServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private FallService fallService;

	@Inject
	private DossierService dossierService;

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionService institutionService;

	@Test
	public void createFallTest() {
		Assert.assertNotNull(fallService);
		Fall fall = TestDataUtil.createDefaultFall();
		fallService.saveFall(fall);

		Collection<Fall> allFalle = fallService.getAllFalle(false);
		Assert.assertEquals(1, allFalle.size());

		Assert.assertNotNull(fallService);
		Fall secondFall = TestDataUtil.createDefaultFall();
		fallService.saveFall(secondFall);

		//Wir erwarten dass unterschiedliche Fallnummern vergeben wurden
		List<Fall> moreFaelle = fallService.getAllFalle(false).stream()
			.sorted(Comparator.comparingLong(Fall::getFallNummer))
			.collect(Collectors.toList());
		Assert.assertEquals(2, moreFaelle.size());
		Assert.assertNotEquals(moreFaelle.get(0).getFallNummer(), moreFaelle.get(1).getFallNummer());
	}

	@Test
	public void changeVerantwortlicherOfFallTest() {
		Dossier dossier = TestDataUtil.createDefaultDossier();
		persistence.persist(dossier.getFall());
		dossier.setGemeinde(TestDataUtil.getTestGemeinde(persistence));
		Dossier savedDossier = dossierService.saveDossier(dossier);

		Optional<Dossier> loadedDossierOptional = dossierService.findDossier(savedDossier.getId());
		Assert.assertTrue(loadedDossierOptional.isPresent());
		Dossier loadedDossier = loadedDossierOptional.get();
		Assert.assertNull(loadedDossier.getVerantwortlicherBG());
		Benutzer benutzerToSet = getDummySuperadmin();
		Benutzer storedBenutzer = persistence.find(Benutzer.class, benutzerToSet.getId());
		loadedDossier.setVerantwortlicherBG(storedBenutzer);

		Dossier updatedDossier = dossierService.saveDossier(loadedDossier);
		Assert.assertNotNull(loadedDossier.getVerantwortlicherBG());
		Assert.assertEquals(benutzerToSet, updatedDossier.getVerantwortlicherBG());

	}

	@Test
	public void removeFallTest() {
		Assert.assertNotNull(fallService);
		Fall fall = TestDataUtil.createDefaultFall();
		fallService.saveFall(fall);
		Assert.assertEquals(1, fallService.getAllFalle(false).size());

		fallService.removeFall(fall, GesuchDeletionCause.USER);
		Assert.assertEquals(0, fallService.getAllFalle(false).size());
	}

	@Test
	public void testCreateFallForGSNoGS() {
		loginAsSachbearbeiterJA();

		final Optional<Fall> fall = fallService.createFallForCurrentGesuchstellerAsBesitzer();
		Assert.assertFalse(fall.isPresent());
	}

	@Test
	public void testCreateFallForGSTwoTimes() {
		loginAsGesuchsteller("gesuchst");

		final Optional<Fall> fall = fallService.createFallForCurrentGesuchstellerAsBesitzer();
		Assert.assertTrue(fall.isPresent());
		Fall persistedFall = fall.get();
		Assert.assertNotNull(persistedFall.getBesitzer());
		Assert.assertEquals("gesuchst", persistedFall.getBesitzer().getUsername());

		final Optional<Fall> fall2 = fallService.createFallForCurrentGesuchstellerAsBesitzer();
		Assert.assertFalse(fall2.isPresent()); // if a fall already exists for this GS it is not created again
	}

	@Test
	public void testCreateFallForTwoDifferentGS() {
		loginAsGesuchsteller("gesuchst");
		final Optional<Fall> fall = fallService.createFallForCurrentGesuchstellerAsBesitzer();
		Assert.assertTrue(fall.isPresent());
		Fall persistedFall = fall.get();
		Assert.assertNotNull(persistedFall.getBesitzer());
		Assert.assertEquals("gesuchst", persistedFall.getBesitzer().getUsername());

		loginAsGesuchsteller("gesuchst2");
		final Optional<Fall> fall2 = fallService.createFallForCurrentGesuchstellerAsBesitzer();
		Assert.assertTrue(fall2.isPresent()); // if a fall already exists for this GS it is not created again
		Fall persistedFall2 = fall2.get();
		Assert.assertNotNull(persistedFall2.getBesitzer());
		Assert.assertEquals("gesuchst2", persistedFall2.getBesitzer().getUsername());
	}

	@Test
	public void testGetEmailAddressForFallFromFall() {
		loginAsGesuchsteller("gesuchst");
		final Optional<Fall> fallOpt = fallService.createFallForCurrentGesuchstellerAsBesitzer();
		Assert.assertTrue(fallOpt.isPresent());
		Fall fall = fallOpt.get();
		Assert.assertNotNull(fall.getBesitzer());
		Assert.assertEquals("e@e", fall.getBesitzer().getEmail());
		Assert.assertEquals("gesuchst", fall.getBesitzer().getUsername());
		Optional<String> emailAddressForFall = fallService.getCurrentEmailAddress(fall.getId());
		Assert.assertTrue(emailAddressForFall.isPresent());
		String email = emailAddressForFall.get();
		Assert.assertEquals("e@e", email);
	}

	@Test
	public void testGetEmailAddressForFallFromGS() {
		loginAsGesuchsteller("gesuchst");
		Gesuchsperiode gesuchsperiode1718 = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode1718, persistence);
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, null, null, gesuchsperiode1718);

		Assert.assertNotNull(gesuch.getGesuchsteller1());
		Assert.assertNotNull(gesuch.getGesuchsteller1().getGesuchstellerJA().getMail());
		Assert.assertNotNull(gesuch.getFall().getBesitzer());
		Assert.assertNotEquals(gesuch.getFall().getBesitzer().getEmail(), gesuch.getGesuchsteller1().getGesuchstellerJA().getMail());

		Optional<String> emailAddressForFall = fallService.getCurrentEmailAddress(gesuch.getFall().getId());
		Assert.assertTrue(emailAddressForFall.isPresent());
		String email = emailAddressForFall.get();
		Assert.assertEquals("test@mailbucket.dvbern.ch", email);
	}

	@Test
	public void testGetEmailAddressForFallFromSozialdienstFall() {
		loginAsGesuchsteller("gesuchst");
		Gesuchsperiode gesuchsperiode1718 = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode1718, persistence);
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, null, null, gesuchsperiode1718);

		TestDataUtil.addSozialdienstToFall(persistence, fallService, gesuch.getFall());

		Assert.assertNotNull(gesuch.getGesuchsteller1());
		Assert.assertNotNull(gesuch.getGesuchsteller1().getGesuchstellerJA().getMail());
		Assert.assertNotNull(gesuch.getFall().getBesitzer());
		Assert.assertNotEquals(gesuch.getFall().getBesitzer().getEmail(), gesuch.getGesuchsteller1().getGesuchstellerJA().getMail());
		Assert.assertNotNull(gesuch.getFall().getSozialdienstFall());

		Optional<String> emailAddressForFall = fallService.getCurrentEmailAddress(gesuch.getFall().getId());
		Assert.assertTrue(emailAddressForFall.isPresent());
		String email = emailAddressForFall.get();
		Assert.assertEquals("sozialmail@mailbucket.dvbern.ch", email);
	}
}
