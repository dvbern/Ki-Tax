/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;

import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Arquillian Tests fuer die Klasse BenutzerService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class BenutzerServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private Persistence persistence;

	@Inject
	private FallService fallService;

	@Test
	public void oneBerechtigung() {
		Benutzer benutzer = TestDataUtil.createDefaultBenutzer();
		benutzer.getBerechtigungen().iterator().next().getGemeindeList().add(TestDataUtil.getGemeindeParis(persistence));
		persistence.merge(benutzer.getMandant());
		persistence.merge(benutzer);

		Set<Berechtigung> berechtigungen = benutzer.getBerechtigungen();
		assertNotNull(berechtigungen);
		assertEquals(1, berechtigungen.size());
	}

	@Test
	public void addBerechtigung() {
		LocalDate AB_ERSTE_BERECHTIGUNG = LocalDate.now();
		Benutzer benutzer = TestDataUtil.createDefaultBenutzer();
		benutzer.getBerechtigungen().iterator().next().getGemeindeList().add(TestDataUtil.getGemeindeParis(persistence));
		benutzer.getCurrentBerechtigung().getGueltigkeit().setGueltigAb(AB_ERSTE_BERECHTIGUNG);
		persistence.merge(benutzer.getMandant());
		benutzer = persistence.merge(benutzer);
		Set<Berechtigung> berechtigungen = benutzer.getBerechtigungen();
		assertNotNull(berechtigungen);
		assertEquals(1, berechtigungen.size());
		Berechtigung firstBerechtigung = berechtigungen.iterator().next();
		assertEquals(AB_ERSTE_BERECHTIGUNG, firstBerechtigung.getGueltigkeit().getGueltigAb());
		assertEquals(Constants.END_OF_TIME, firstBerechtigung.getGueltigkeit().getGueltigBis());
		assertEquals(UserRole.ADMIN_BG, firstBerechtigung.getRole());

		// Eine zweite Berechtigung erfassen
		LocalDate AB_ZWEITE_BERECHTIGUNG = LocalDate.now().plusMonths(1);
		Berechtigung secondBerechtigung = new Berechtigung();
		secondBerechtigung.setBenutzer(benutzer);
		secondBerechtigung.setRole(UserRole.SACHBEARBEITER_BG);
		secondBerechtigung.getGueltigkeit().setGueltigAb(AB_ZWEITE_BERECHTIGUNG);
		secondBerechtigung.getGemeindeList().add(TestDataUtil.getGemeindeParis(persistence));
		benutzer.getBerechtigungen().add(secondBerechtigung);
		benutzer = benutzerService.saveBenutzerBerechtigungen(benutzer, false);

		Set<Berechtigung> berechtigungen2 = benutzer.getBerechtigungen();
		assertNotNull(berechtigungen2);
		assertEquals(2, berechtigungen2.size());
		Iterator<Berechtigung> iterator = berechtigungen2.iterator();
		Berechtigung firstBerechtigung2 = iterator.next();
		Berechtigung secondBerechtigung2 = iterator.next();

		assertEquals(AB_ERSTE_BERECHTIGUNG, firstBerechtigung2.getGueltigkeit().getGueltigAb());
		assertEquals(AB_ZWEITE_BERECHTIGUNG.minusDays(1), firstBerechtigung2.getGueltigkeit().getGueltigBis());
		assertEquals(AB_ZWEITE_BERECHTIGUNG, secondBerechtigung2.getGueltigkeit().getGueltigAb());
		assertEquals(Constants.END_OF_TIME, secondBerechtigung2.getGueltigkeit().getGueltigBis());
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	public void handleAbgelaufeneBerechtigung() {
		LocalDate AB_ERSTE_BERECHTIGUNG = LocalDate.now().minusYears(1);
		Benutzer benutzer = TestDataUtil.createDefaultBenutzer();
		benutzer.getBerechtigungen().iterator().next().getGemeindeList().add(TestDataUtil.getGemeindeParis(persistence));
		benutzer.getCurrentBerechtigung().getGueltigkeit().setGueltigAb(AB_ERSTE_BERECHTIGUNG);
		persistence.merge(benutzer.getMandant());
		benutzer = persistence.merge(benutzer);

		// Timer durchlaufen lassen: Es ist immer noch dieselbe Berechtigung aktiv
		benutzerService.handleAbgelaufeneRollen(LocalDate.now());
		Optional<Benutzer> benutzerOptional = benutzerService.findBenutzer(benutzer.getUsername());
		Berechtigung currentBerechtigung = benutzerOptional.get().getCurrentBerechtigung();
		assertEquals(AB_ERSTE_BERECHTIGUNG, currentBerechtigung.getGueltigkeit().getGueltigAb());
		assertEquals(Constants.END_OF_TIME, currentBerechtigung.getGueltigkeit().getGueltigBis());

		// Eine zweite Berechtigung erfassen
		LocalDate AB_ZWEITE_BERECHTIGUNG = LocalDate.now().minusDays(1);
		Berechtigung secondBerechtigung = new Berechtigung();
		secondBerechtigung.setBenutzer(benutzer);
		secondBerechtigung.setRole(UserRole.SACHBEARBEITER_BG);
		secondBerechtigung.getGueltigkeit().setGueltigAb(AB_ZWEITE_BERECHTIGUNG);
		secondBerechtigung.getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		secondBerechtigung.getGemeindeList().add(TestDataUtil.getGemeindeParis(persistence));
		benutzer.getBerechtigungen().add(secondBerechtigung);
		benutzer = benutzerService.saveBenutzerBerechtigungen(benutzer, false);
		Set<Berechtigung> berechtigungen = benutzer.getBerechtigungen();
		assertEquals(2, berechtigungen.size());

		// Timer durchlaufen lassen: Es ist jetzt die neue Berechtigung aktiv
		benutzerService.handleAbgelaufeneRollen(LocalDate.now());
		Optional<Benutzer> benutzerOptional2 = benutzerService.findBenutzer(benutzer.getUsername());
		Berechtigung currentBerechtigung2 = benutzerOptional2.get().getCurrentBerechtigung();
		assertEquals(AB_ZWEITE_BERECHTIGUNG, currentBerechtigung2.getGueltigkeit().getGueltigAb());
		assertEquals(Constants.END_OF_TIME, currentBerechtigung2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void createAdminTraegerschaftByEmail() {
		Traegerschaft traegerschaft = TestDataUtil.createDefaultTraegerschaft();
		persistence.persist(traegerschaft);
		final String adminMail = "traegerschaft@mailbucket.dvbern.ch";
		final Benutzer adminTraegerschaft = benutzerService.createAdminTraegerschaftByEmail(adminMail, traegerschaft);

		assertCommonBenutzerFields(adminMail, adminTraegerschaft);

		assertEquals(traegerschaft, adminTraegerschaft.getTraegerschaft());
		assertEquals(UserRole.ADMIN_TRAEGERSCHAFT, adminTraegerschaft.getRole());
		assertNotNull(adminTraegerschaft.getCurrentBerechtigung());
		assertTrue(adminTraegerschaft.getCurrentBerechtigung().getGemeindeList().isEmpty());
		assertNull(adminTraegerschaft.getInstitution());
	}

	@Test
	public void createAdminInstitutionByEmail() {
		Institution institution = TestDataUtil.createAndPersistDefaultInstitution(persistence);
		final String adminMail = "institution@mailbucket.dvbern.ch";
		final Benutzer adminInstitution = benutzerService.createAdminInstitutionByEmail(adminMail, institution);

		assertCommonBenutzerFields(adminMail, adminInstitution);

		assertEquals(institution, adminInstitution.getInstitution());
		assertEquals(UserRole.ADMIN_INSTITUTION, adminInstitution.getRole());
		assertNotNull(adminInstitution.getCurrentBerechtigung());
		assertTrue(adminInstitution.getCurrentBerechtigung().getGemeindeList().isEmpty());
		assertNull(adminInstitution.getTraegerschaft());
	}

	@Test
	public void createAdminGemeindeByEmail() {
		Gemeinde gemeinde = TestDataUtil.getGemeindeParis(persistence);
		final String adminMail = "gemeinde@mailbucket.dvbern.ch";
		final Benutzer adminGemeinde = benutzerService.createAdminGemeindeByEmail(adminMail, UserRole.ADMIN_GEMEINDE, gemeinde);

		assertCommonBenutzerFields(adminMail, adminGemeinde);

		assertEquals(UserRole.ADMIN_GEMEINDE, adminGemeinde.getRole());
		assertNotNull(adminGemeinde.getCurrentBerechtigung());
		assertEquals(1, adminGemeinde.getCurrentBerechtigung().getGemeindeList().size());
		assertEquals(gemeinde, adminGemeinde.getCurrentBerechtigung().getGemeindeList().iterator().next());
		assertNull(adminGemeinde.getInstitution());
		assertNull(adminGemeinde.getTraegerschaft());
	}

	@Test
	public void einladenGemeindeWrongStatus() {
		Benutzer benutzer = TestDataUtil.createBenutzerSCH();
		benutzer.setMandant(getDummySuperadmin().getMandant());
		benutzer.setStatus(BenutzerStatus.AKTIV);

		Gemeinde gemeinde = TestDataUtil.getGemeindeParis(persistence);

		Einladung einladung = Einladung.forGemeinde(benutzer, gemeinde);

		try {
			benutzerService.einladen(einladung);
			fail(
				"It should throw a EbeguRuntimeException because AKTIV is not a valid status. It must be "
					+ "EINGELADEN");
		} catch (EbeguRuntimeException e) {
			// nop
		}

	}

	@Test
	public void einladenGemeindeWrongUserWithoutGemeinde() {
		LocaleThreadLocal.set(Constants.DEFAULT_LOCALE);
		Benutzer benutzer = TestDataUtil.createBenutzerSCH();
		benutzer.setStatus(BenutzerStatus.EINGELADEN);
		benutzer.setMandant(getDummySuperadmin().getMandant());

		Gemeinde gemeinde = TestDataUtil.getGemeindeParis(persistence);

		Einladung einladung = Einladung.forGemeinde(benutzer, gemeinde);

		try {
			benutzerService.einladen(einladung);
			fail("It should throw a ConstraintViolationException because the user must have a Gemeinde");
		} catch (EJBTransactionRolledbackException e) {
			// nop
		}
	}

	private void assertCommonBenutzerFields(String adminMail, Benutzer adminTraegerschaft) {
		assertEquals(adminMail, adminTraegerschaft.getUsername());
		assertEquals(adminMail, adminTraegerschaft.getEmail());
		assertEquals(Constants.UNKNOWN, adminTraegerschaft.getNachname());
		assertEquals(Constants.UNKNOWN, adminTraegerschaft.getVorname());
		assertEquals(BenutzerStatus.EINGELADEN, adminTraegerschaft.getStatus());
	}
}
