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

package ch.dvbern.ebegu.tests.validations;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.validators.CheckBerechtigungGemeindeValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests fuer {@link CheckBerechtigungGemeindeValidator}
 */
class CheckBerechtigungGemeindeValidatorTest {

	private final CheckBerechtigungGemeindeValidator validator = new CheckBerechtigungGemeindeValidator();
	private final Mandant mandant = new Mandant();
	private final Gemeinde gemeinde = new Gemeinde();

	@Test
	void checkGemeindeAbhaengigeRollenMitGemeindeValid() {
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.ADMIN_BG, true).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_BG, true).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.ADMIN_TS, true).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_TS, true).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.JURIST, true).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.REVISOR, true).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.STEUERAMT, true).getCurrentBerechtigung(), null));
	}

	@Test
	void checkGemeindeAbhaengigeRollenOhneGemeindeInvalid() {
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.ADMIN_BG, false).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_BG, false).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.ADMIN_TS, false).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_TS, false).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.JURIST, false).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.REVISOR, false).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.STEUERAMT, false).getCurrentBerechtigung(), null));
	}


	@Test
	void checkGemeindeUnabhaengigeRollenOhneGemeindeValid() {
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.SUPER_ADMIN, false).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.GESUCHSTELLER, false).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, false).getCurrentBerechtigung(), null));
		Assertions.assertTrue(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_INSTITUTION, false).getCurrentBerechtigung(), null));
	}

	@Test
	void checkGemeindeUnabhaengigeRollenMitGemeindeInvalid() {
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.SUPER_ADMIN, true).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.GESUCHSTELLER, true).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, true).getCurrentBerechtigung(), null));
		Assertions.assertFalse(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_INSTITUTION, true).getCurrentBerechtigung(), null));
	}

	private Benutzer createBenutzer(UserRole role, boolean addGemeinde) {
		Benutzer benutzer = TestDataUtil.createBenutzer(role, Constants.ANONYMOUS_USER_USERNAME, null, null, mandant, null, null);
		if (addGemeinde) {
			benutzer.getBerechtigungen().iterator().next().getGemeindeList().add(gemeinde);
		}
		return benutzer;
	}
}
