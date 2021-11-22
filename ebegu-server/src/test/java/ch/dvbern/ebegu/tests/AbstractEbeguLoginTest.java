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

import ch.dvbern.ebegu.test.TestUserIds;
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
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.test.util.JBossLoginContextFactory.createLoginContext;

/**
 * Diese Klasse loggt vor jeder testmethode als superadmin ein und danach wieder aus.
 * Zudem wird der superadmin in der dp erstellt
 */
@UsingDataSet("datasets/default-mandant.xml")
public abstract class AbstractEbeguLoginTest extends AbstractEbeguTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractEbeguLoginTest.class);
	public static final String SUPERADMIN_NAME = "superadmin";
	public static final String SUPERADMIN_ID = "a2f9f847-47af-11ec-a0cc-b89a2ae4a038";
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
		dummyAdmin = TestDataUtil.createDummySuperAdmin(persistence, mandant, SUPERADMIN_NAME, "superadmin", SUPERADMIN_ID);
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
		loginContext = JBossLoginContextFactory.createLoginContext(SUPERADMIN_ID, "superadmin");
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

	protected Benutzer loginAsGesuchsteller1() {
		return this.loginAsGesuchsteller("gesuchst", TestUserIds.GESUCHSTELLER_1);
	}

	protected Benutzer loginAsGesuchsteller2() {
		return this.loginAsGesuchsteller("gesuchst2", TestUserIds.GESUCHSTELLER_2);
	}

	private Benutzer loginAsGesuchsteller(String username, String userId) {
		assert username.equals("gesuchst") || username.equals("gesuchst2");
		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer user = createOrFindBenutzer(UserRole.GESUCHSTELLER, username, null, null, mandant, userId);
		try {
			createLoginContext(userId, username).login();
		} catch (LoginException e) {
			LOG.error("could not login as gesuchsteller {} for tests", username);
		}
		return persistence.merge(user);
		//theoretisch sollten wir wohl zuerst ausloggen bevor wir wieder einloggen aber es scheint auch so zu gehen
	}

	protected Benutzer loginAsSchulamt() {
		try {
			createLoginContext(TestUserIds.SCHULAMT_SACHBEARBEITERIN, "schulamt").login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter schulamt for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer schulamt = createOrFindBenutzer(UserRole.SACHBEARBEITER_TS, "schulamt", null, null, mandant, TestUserIds.SCHULAMT_SACHBEARBEITERIN);
		return persistence.merge(schulamt);
	}

	protected Benutzer loginAsAdminSchulamt() {
		try {
			createLoginContext(TestUserIds.SCHULAMT_ADMIN, "schulamtadmin").login();
		} catch (LoginException e) {
			LOG.error("could not login as admin schulamt for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer schulamt = createOrFindBenutzer(UserRole.ADMIN_TS, "schulamtadmin", null, null, mandant, TestUserIds.SCHULAMT_ADMIN);
		return persistence.merge(schulamt);
	}

	protected void loginAsSteueramt() {
		try {
			createLoginContext(TestUserIds.STEUERAMT, "steueramt").login();
		} catch (LoginException e) {
			LOG.error("could not login as steueramt for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer steueramt = createOrFindBenutzer(UserRole.STEUERAMT, "steueramt", null, null, mandant, TestUserIds.STEUERAMT);
		persistence.merge(steueramt);
	}

	protected Benutzer loginAsSachbearbeiterJA() {
		try {
			createLoginContext(TestUserIds.BG_SACHBEARBEITERIN, "saja").login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt saja for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer saja = createOrFindBenutzer(UserRole.SACHBEARBEITER_BG, "saja", null, null, mandant, TestUserIds.BG_SACHBEARBEITERIN);
		persistence.merge(saja);
		return saja;
	}

	protected Benutzer loginAsJurist() {
		try {
			createLoginContext(TestUserIds.JURISTIN, "jurist").login();
		} catch (LoginException e) {
			LOG.error("could not login as jurist for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer jurist = createOrFindBenutzer(UserRole.JURIST, "jurist", null, null, mandant, TestUserIds.JURISTIN);
		persistence.merge(jurist);
		return jurist;
	}

	protected void loginAsAdmin() {
		try {
			createLoginContext(TestUserIds.BG_ADMIN, "admin").login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt admin for tests");
		}

		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer admin = createOrFindBenutzer(UserRole.ADMIN_BG, "admin", null, null, mandant, TestUserIds.BG_ADMIN);
		persistence.merge(admin);
	}
	
	protected Benutzer loginAsSachbearbeiterInst(Institution institutionToSet) {
		return this.loginAsSachbearbeiterInst("sainst", institutionToSet, TestUserIds.INSTITUTION_SACHBEARBEITER);
	}
	
	protected Benutzer loginAsSachbearbeiterInst2(Institution institutionToSet) {
		return this.loginAsSachbearbeiterInst("sainst2", institutionToSet, TestUserIds.INSTITUTION_SACHBEARBEITER2);
	}

	private Benutzer loginAsSachbearbeiterInst(String username, Institution institutionToSet, String id) {
		Benutzer user = createOrFindBenutzer(UserRole.SACHBEARBEITER_INSTITUTION,
				username,
				null,
				institutionToSet,
				institutionToSet.getMandant(),
				id);
		user = persistence.merge(user);
		try {
			createLoginContext(id, username).login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt {} for tests", username);
		}
		return user;
		//theoretisch sollten wir wohl zuerst ausloggen bevor wir wieder einloggen aber es scheint auch so zu gehen
	}

	protected Benutzer loginAsSachbearbeiterTraegerschaft(Traegerschaft traegerschaft) {
		Mandant mandant = persistence.find(Mandant.class, Constants.DEFAULT_MANDANT_ID);
		Benutzer user = createOrFindBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
				"satraeg", traegerschaft, null, mandant,
				TestUserIds.TRAEGERSCHAFT_SACHBEARBEITER);
		user = persistence.merge(user);
		try {
			createLoginContext(TestUserIds.TRAEGERSCHAFT_SACHBEARBEITER, "satraeg").login();
		} catch (LoginException e) {
			LOG.error("could not login as sachbearbeiter jugendamt {} for tests", "satraeg");
		}
		return user;
		//theoretisch sollten wir wohl zuerst ausloggen bevor wir wieder einloggen aber es scheint auch so zu gehen
	}

	private Benutzer createOrFindBenutzer(
			UserRole role,
			String userName,
			@Nullable Traegerschaft traegerschaft,
			@Nullable Institution institution,
			@Nullable Mandant mandant,
			String userId) {
		Optional<Benutzer> benutzer = benutzerService.findBenutzerById(userId);
		return benutzer.orElseGet(() -> {
			assert mandant != null;
			Benutzer toReturn = TestDataUtil.createBenutzerWithDefaultGemeinde(role, userName, traegerschaft, institution, mandant, persistence, null, null, userId);
			return toReturn;
		});
	}
}
