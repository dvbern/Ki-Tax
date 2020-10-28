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

package ch.dvbern.ebegu.tests.services;

import javax.annotation.Nonnull;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.tests.AbstractEbeguLoginTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Arquillian Tests fuer die Klasse BenutzerService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class AuthorizerTest extends AbstractEbeguLoginTest {

	@Inject
	private Persistence persistence;
	@Inject
	private Authorizer authorizer;

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchAllowedGemeindeAdmin() {
		loginAsAdmin();

		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_PARIS_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		checkReadAuthorizationGesuchAllowed(gesuch, "The Admin can see the Gesuch because it belongs to his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchNotAllowedGemeindeAdmin() {
		loginAsAdmin();

		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_LONDON_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		checkReadAuthorizationGesuchNotAllowed(gesuch, "The Admin cannot see the Gesuch because it doesn't belong to his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchNotGemeindeAbhaengig() throws LoginException {
		loginAsSuperadmin();

		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_LONDON_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		checkReadAuthorizationGesuchAllowed(gesuch, "SuperAdmin can see all Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchAllowedGemeindeJurist() {
		loginAsAdmin();

		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_PARIS_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		loginAsJurist();
		checkReadAuthorizationGesuchAllowed(gesuch, "The Jurist can see the Gesuch because it belongs to his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchNotAllowedGemeindeJurist() {
		loginAsAdmin();

		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_LONDON_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		loginAsJurist();
		checkReadAuthorizationGesuchNotAllowed(gesuch, "The Jurist cannot see the Gesuch because it doesn't belong to his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchAllowedGemeindeSchulamt() {
		loginAsSchulamt();

		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_PARIS_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		checkReadAuthorizationGesuchAllowed(gesuch, "The SCH-User can see the Gesuch because it belongs to his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchNotAllowedGemeindeSchulamt() {
		loginAsSchulamt();

		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_LONDON_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		checkReadAuthorizationGesuchNotAllowed(gesuch, "The SCH-User cannot see the Gesuch because it doesn't belong to his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchAllowedGemeindeSteueramt() {
		loginAsAdmin();
		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_PARIS_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		gesuch.setStatus(AntragStatus.PRUEFUNG_STV);
		persistence.merge(gesuch);
		loginAsSteueramt();
		checkReadAuthorizationGesuchAllowed(gesuch, "The Steueramt-User can see the Gesuch because it belongs to his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedGesuchNotAllowedGemeindeSteueramt() {
		loginAsAdmin();
		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_LONDON_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		gesuch.setStatus(AntragStatus.PRUEFUNG_STV);
		persistence.merge(gesuch);
		loginAsSteueramt();
		checkReadAuthorizationGesuchNotAllowed(gesuch, "The Steueramt-User cannot see the Gesuch because it doesn't belong to his Gemeinde");
	}

	/**
	 * An exception should be thrown when the user doesn't belong to the gemeinde of the gesuch
	 */
	private void checkReadAuthorizationGesuchNotAllowed(Gesuch gesuch, String message) {
		try {
			authorizer.checkReadAuthorization(gesuch);
			Assert.fail(message);
		} catch (EJBAccessException e) {
			// nop
		} catch (Exception e) {
			Assert.fail("It should only throw an EJBAccessException");
		}
	}

	/**
	 * An exception shouldn't be thrown when the user belongs to the gemeinde of the gesuch
	 */
	private void checkReadAuthorizationGesuchAllowed(Gesuch gesuch, String message) {
		try {
			authorizer.checkReadAuthorization(gesuch);

		} catch(Exception e) {
			Assert.fail(message);
		}
	}


	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedFallAllowedGemeindeAdmin() {
		loginAsAdmin();
		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_PARIS_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		checkReadAuthorizationFallAllowed(gesuch, "The JA-User should be able to see the Fall because it has at least one Dossier of his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedFallNotAllowedGemeindeAdmin() {
		loginAsAdmin();
		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_LONDON_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		checkReadAuthorizationFallNotAllowed(gesuch, "The JA-User shouldn't be able to see the Fall because it hasn't got any Dossier of his Gemeinde");
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void testIsReadAuthorizedFallAllowedNotGemeindeAbhaengig() throws LoginException {
		loginAsSuperadmin();
		final Gemeinde gemeinde = persistence.find(Gemeinde.class, TestDataUtil.GEMEINDE_PARIS_ID);
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, gemeinde);
		checkReadAuthorizationFallAllowed(gesuch, "The Superadmin should be able to see all Faelle");
	}

	/**
	 * An exception shouldn't be thrown when the user belongs to the gemeinde of any the dossiers of the fall
	 */
	private void checkReadAuthorizationFallAllowed(Gesuch gesuch, @Nonnull String message) {
		try {
			authorizer.checkReadAuthorizationFall(gesuch.getFall());

		} catch(Exception e) {
			Assert.fail(message);
		}
	}

	/**
	 * An exception should be thrown when the user doesn't belong to the gemeinde of any the dossiers of the fall
	 */
	private void checkReadAuthorizationFallNotAllowed(Gesuch gesuch, @Nonnull String message) {
		try {
			authorizer.checkReadAuthorizationFall(gesuch.getFall());
			Assert.fail(message);

		} catch(EJBAccessException | EJBTransactionRolledbackException e) {
			// nop
		} catch (Exception e) {
			Assert.fail("It should only throw an EJBAccessException or EJBTransactionRolledbackException");
		}
	}
}
