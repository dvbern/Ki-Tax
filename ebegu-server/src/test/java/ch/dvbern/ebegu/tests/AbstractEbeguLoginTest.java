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
import java.time.Month;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.BenutzerService;
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
public abstract class AbstractEbeguLoginTest extends AbstractEbeguTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractEbeguLoginTest.class);
	public static final String SUPERADMIN_NAME = "superadmin";
	private LoginContext loginContext;

	@Inject
	private Persistence persistence;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	private Benutzer dummyAdmin;

	@Inject
	private BenutzerService benutzerService;

	@Before
	public void performLogin() {
		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		dummyAdmin = TestDataUtil.createDummySuperAdmin(persistence, mandant, SUPERADMIN_NAME, SUPERADMIN_NAME);
		try {
			loginAsSuperadmin();
		} catch (LoginException ex) {
			LOG.error("Konnte dummy login nicht vornehmen fuer ArquillianTests ", ex);
		}
		// Fuer die Tests soll Bern/Paris bereits nach ASIV funktionieren, wir setzen die Daten in die Vergangeheit
		LocalDate stadtBernStartDatumAsiv = LocalDate.of(2000, Month.JANUARY, 1);
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.STADT_BERN_ASIV_START_DATUM, Constants.DATE_FORMATTER.format(stadtBernStartDatumAsiv), mandant);
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.STADT_BERN_ASIV_CONFIGURED, "true", mandant);
	}

	protected void loginAsSuperadmin() throws LoginException {
		loginContext = JBossLoginContextFactory.createLoginContext(SUPERADMIN_NAME, SUPERADMIN_NAME);
		loginContext.login();
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

	public Benutzer getDummySuperadmin() {
		return dummyAdmin;
	}

	protected Benutzer loginAsGesuchsteller(String username) {
		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer user = createOrFindBenutzer(UserRole.GESUCHSTELLER, username, null, null, mandant);
		user = persistence.merge(user);
		try {
			createLoginContext(username, username).login();
		} catch (LoginException e) {
			LOG.error("could not login as gesuchsteller {} for tests", username);
		}
		return user;
		//theoretisch sollten wir wohl zuerst ausloggen bevor wir wieder einloggen aber es scheint auch so zu gehen
	}

	protected Benutzer loginAsSchulamt() {
		try {
			createLoginContext("schulamt", "schulamt").login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter schulamt for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer schulamt = createOrFindBenutzer(UserRole.SACHBEARBEITER_TS, "schulamt", null, null, mandant);
		return persistence.merge(schulamt);
	}

	protected Benutzer loginAsAdminSchulamt() {
		try {
			createLoginContext("schulamtadmin", "schulamtadmin").login();
		} catch (LoginException e) {
			LOG.error("could not login as admin schulamt for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer schulamt = createOrFindBenutzer(UserRole.ADMIN_TS, "schulamtadmin", null, null, mandant);
		return persistence.merge(schulamt);
	}

	protected void loginAsSteueramt() {
		try {
			createLoginContext("steueramt", "steueramt").login();
		} catch (LoginException e) {
			LOG.error("could not login as steueramt for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer steueramt = createOrFindBenutzer(UserRole.STEUERAMT, "steueramt", null, null, mandant);
		persistence.merge(steueramt);
	}

	protected Benutzer loginAsSachbearbeiterJA() {
		try {
			createLoginContext("saja", "saja").login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt saja for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer saja = createOrFindBenutzer(UserRole.SACHBEARBEITER_BG, "saja", null, null, mandant);
		persistence.merge(saja);
		return saja;
	}

	protected Benutzer loginAsJurist() {
		try {
			createLoginContext("jurist", "jurist").login();
		} catch (LoginException e) {
			LOG.error("could not login as jurist for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer jurist = createOrFindBenutzer(UserRole.JURIST, "jurist", null, null, mandant);
		persistence.merge(jurist);
		return jurist;
	}

	protected void loginAsAdmin() {
		try {
			createLoginContext("admin", "admin").login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt admin for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer admin = createOrFindBenutzer(UserRole.ADMIN_BG, "admin", null, null, mandant);
		persistence.merge(admin);
	}

	protected Benutzer loginAsSachbearbeiterInst(String username, Institution institutionToSet) {
		Benutzer user = createOrFindBenutzer(UserRole.SACHBEARBEITER_INSTITUTION, username, null, institutionToSet, institutionToSet.getMandant());
		user = persistence.merge(user);
		try {
			createLoginContext(username, username).login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt {} for tests", username);
		}
		return user;
		//theoretisch sollten wir wohl zuerst ausloggen bevor wir wieder einloggen aber es scheint auch so zu gehen
	}

	protected Benutzer loginAsSachbearbeiterTraegerschaft(String username, Traegerschaft traegerschaft) {
		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer user = createOrFindBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, username, traegerschaft, null, mandant);
		user = persistence.merge(user);
		try {
			createLoginContext(username, username).login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt {} for tests", username);
		}
		return user;
		//theoretisch sollten wir wohl zuerst ausloggen bevor wir wieder einloggen aber es scheint auch so zu gehen
	}

	private Benutzer createOrFindBenutzer(UserRole role, String userName, @Nullable Traegerschaft traegerschaft, @Nullable Institution institution, @Nullable Mandant mandant) {
		Optional<Benutzer> benutzer = benutzerService.findBenutzer(userName);
		return benutzer.orElseGet(() -> {
			assert mandant != null;
			return TestDataUtil.createBenutzerWithDefaultGemeinde(role, userName, traegerschaft, institution, mandant, persistence, null, null);
		});
	}
}
