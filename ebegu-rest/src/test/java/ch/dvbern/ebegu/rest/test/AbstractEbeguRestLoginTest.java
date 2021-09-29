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

package ch.dvbern.ebegu.rest.test;

import java.time.LocalDate;
import java.time.Month;

import javax.inject.Inject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.JBossLoginContextFactory;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.test.util.JBossLoginContextFactory.createLoginContext;

/**
 * Diese Klasse loggt vor jeder testmethode als superadmin ein und danach wieder aus.
 * Zudem wird der superadmin in der dp erstellt
 */
public abstract class AbstractEbeguRestLoginTest extends AbstractEbeguRestTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractEbeguRestLoginTest.class);
	public static final String SUPERADMIN_NAME = "superadmin";
	public LoginContext loginContext;

	@Inject
	private Persistence persistence;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	private Benutzer dummyAdmin;

	@Before
	public void performLogin() {
		Mandant mandant = TestDataUtil.createDefaultMandant();
		TestDataUtil.saveMandantIfNecessary(persistence, mandant);
		dummyAdmin = TestDataUtil.createDummySuperAdmin(persistence, mandant, SUPERADMIN_NAME, SUPERADMIN_NAME);
		try {
			loginContext = JBossLoginContextFactory.createLoginContext(SUPERADMIN_NAME, SUPERADMIN_NAME);
			loginContext.login();
		} catch (LoginException ex) {
			LOG.error("Konnte dummy login nicht vornehmen fuer ArquillianTests ", ex);
		}
		// Fuer die Tests soll Bern/Paris bereits nach ASIV funktionieren, wir setzen die Daten in die Vergangeheit
		LocalDate stadtBernStartDatumAsiv = LocalDate.of(2000, Month.JANUARY, 1);
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.STADT_BERN_ASIV_START_DATUM, Constants.DATE_FORMATTER.format(stadtBernStartDatumAsiv), mandant);
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.STADT_BERN_ASIV_CONFIGURED, "true", mandant);
	}

	@After
	public void performLogout() {
		try {
			if (loginContext != null) {
				loginContext.logout();
			}
		} catch (LoginException e) {
			LOG.error("Konnte dummy loginnicht ausloggen ", e);
		}
	}

	protected Benutzer loginAsSuperadmin() {
		try {
			createLoginContext(SUPERADMIN_NAME, SUPERADMIN_NAME).login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt saja for tests");
		}
		return dummyAdmin;
	}

	protected Benutzer loginAsSachbearbeiterJA() {
		try {
			createLoginContext("saja", "saja").login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt saja for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, "e3736eb8-6eef-40ef-9e52-96ab48d8f220");
		Benutzer saja = TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SACHBEARBEITER_BG, "saja", null, null, mandant, persistence, null, null);
		persistence.persist(saja);
		return saja;
	}

	public Benutzer getDummySuperadmin() {
		return dummyAdmin;
	}
}
