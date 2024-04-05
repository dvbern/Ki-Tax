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

package ch.dvbern.ebegu.tests.validations;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.validators.CheckBerechtigungInstitutionTraegerschaftValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests fuer {@link CheckBerechtigungInstitutionTraegerschaftValidator}
 */
public class CheckBerechtigungInstitutionTraegerschaftValidatorTest {

	private final CheckBerechtigungInstitutionTraegerschaftValidator validator =
		new CheckBerechtigungInstitutionTraegerschaftValidator();
	private final Mandant mandant = new Mandant();

	@Test
	public void testCheckBenutzerRoleInstitutionWithoutInstitution() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_INSTITUTION,
			Constants.ANONYMOUS_USER_USERNAME, null, null, mandant, null, null);
		assertFalse(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleInstitutionWithInstitution() {
		final Institution institution = new Institution();
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_INSTITUTION, Constants.ANONYMOUS_USER_USERNAME,
			null, institution, mandant,
			null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleTraegerschaftWithoutTraegerschaft() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, Constants.ANONYMOUS_USER_USERNAME,
			null, null, mandant, null, null);
		assertFalse(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleTraegerschaftWithTraegerschaft() {
		final Traegerschaft traegerschaft = new Traegerschaft();
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, Constants.ANONYMOUS_USER_USERNAME,
			traegerschaft, null, mandant
			, null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleAdminNoInstitutionTraegerschaft() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.ADMIN_BG, Constants.ANONYMOUS_USER_USERNAME, null,
			null, mandant, null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleGesuchstellerNoInstitutionTraegerschaft() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.GESUCHSTELLER, Constants.ANONYMOUS_USER_USERNAME,
			null, null, mandant, null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleJuristNoInstitutionTraegerschaft() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.JURIST, Constants.ANONYMOUS_USER_USERNAME,
			null, null, mandant, null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleSchulamtNoInstitutionTraegerschaft() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_TS, Constants.ANONYMOUS_USER_USERNAME,
			null, null, mandant, null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleRevisorNoInstitutionTraegerschaft() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.REVISOR, Constants.ANONYMOUS_USER_USERNAME,
			null, null, mandant, null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleJANoInstitutionTraegerschaft() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_BG, Constants.ANONYMOUS_USER_USERNAME,
			null, null, mandant, null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}

	@Test
	public void testCheckBenutzerRoleSteueramtNoInstitutionTraegerschaft() {
		Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.STEUERAMT, Constants.ANONYMOUS_USER_USERNAME,
			null, null, mandant, null, null);
		assertTrue(validator.isValid(benutzer.getCurrentBerechtigung(), null));
	}
}
