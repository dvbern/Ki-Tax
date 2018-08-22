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
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.ebegu.validators.CheckBerechtigungGemeindeValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests fuer {@link CheckBerechtigungGemeindeValidator}
 */
public class CheckBerechtigungGemeindeValidatorTest {

	private CheckBerechtigungGemeindeValidator validator;
	private Mandant mandant = new Mandant();
	private Gemeinde gemeinde = new Gemeinde();

	@Before
	public void setUp() {
		validator = new CheckBerechtigungGemeindeValidator();
	}

	@Test
	public void checkGemeindeAbhaengigeRollenMitGemeindeValid() {
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.ADMIN_BG, true).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_BG, true).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.ADMIN_TS, true).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.SCHULAMT, true).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.JURIST, true).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.REVISOR, true).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.STEUERAMT, true).getCurrentBerechtigung(), null));
	}

	@Test
	public void checkGemeindeAbhaengigeRollenOhneGemeindeInvalid() {
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.ADMIN_BG, false).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_BG, false).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.ADMIN_TS, false).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.SCHULAMT, false).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.JURIST, false).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.REVISOR, false).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.STEUERAMT, false).getCurrentBerechtigung(), null));
	}


	@Test
	public void checkGemeindeUnabhaengigeRollenOhneGemeindeValid() {
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.SUPER_ADMIN, false).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.GESUCHSTELLER, false).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, false).getCurrentBerechtigung(), null));
		Assert.assertTrue(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_INSTITUTION, false).getCurrentBerechtigung(), null));
	}

	@Test
	public void checkGemeindeUnabhaengigeRollenMitGemeindeInvalid() {
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.SUPER_ADMIN, true).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.GESUCHSTELLER, true).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, true).getCurrentBerechtigung(), null));
		Assert.assertFalse(validator.isValid(createBenutzer(UserRole.SACHBEARBEITER_INSTITUTION, true).getCurrentBerechtigung(), null));
	}

	private Benutzer createBenutzer(UserRole role, boolean addGemeinde) {
		Benutzer benutzer = TestDataUtil.createBenutzer(role, "anonymous", null, null, mandant);
		if (addGemeinde) {
			benutzer.getBerechtigungen().iterator().next().getGemeindeList().add(gemeinde);
		}
		return benutzer;
	}
}
