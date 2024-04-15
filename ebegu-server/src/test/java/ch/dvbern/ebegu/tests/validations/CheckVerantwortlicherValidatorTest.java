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
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherBGValidationGroup;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherTSValidationGroup;
import ch.dvbern.ebegu.validators.CheckVerantwortlicherBG;
import ch.dvbern.ebegu.validators.CheckVerantwortlicherTS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.tests.util.validation.ViolationMatchers.violatesAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
class CheckVerantwortlicherValidatorTest extends AbstractValidatorTest {

	private Benutzer schUser = null;
	private Benutzer jaUser = null;
	private Benutzer jaAdmin = null;
	private Benutzer schAdmin = null;

	@BeforeEach
	void setUp() {
		Mandant mandant = TestDataUtil.getMandantKantonBern();
		schUser = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_TS, "userSCH", null, null, mandant, null, null);
		jaUser = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_BG, "userJA", null, null, mandant, null, null);
		jaAdmin = TestDataUtil.createBenutzer(UserRole.ADMIN_BG, "adminJA", null, null, mandant, null, null);
		schAdmin = TestDataUtil.createBenutzer(UserRole.ADMIN_TS, "adminSCH", null, null, mandant, null, null);
	}

	@Test
	void testCheckVerantwortlicherNormalUsers() {
		testCheckVerantwortlicherUsers(jaUser, schUser);
	}

	@Test
	void testCheckVerantwortlicherAdminUsers() {
		testCheckVerantwortlicherUsers(jaAdmin, schAdmin);
	}

	private void testCheckVerantwortlicherUsers(Benutzer user1, Benutzer user2) {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(user1);
		dossier.setVerantwortlicherTS(user2);

		assertThat(
			validate(dossier, ChangeVerantwortlicherBGValidationGroup.class),
			not(violatesAnnotation(CheckVerantwortlicherBG.class)));
		assertThat(
			validate(dossier, ChangeVerantwortlicherTSValidationGroup.class),
			not(violatesAnnotation(CheckVerantwortlicherTS.class)));
	}

	@Test
	void testCheckVerantwortlicherNullUsers() {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(null);
		dossier.setVerantwortlicherTS(null);

		assertThat(
			validate(dossier, ChangeVerantwortlicherBGValidationGroup.class),
			not(violatesAnnotation(CheckVerantwortlicherBG.class)));
		assertThat(
			validate(dossier, ChangeVerantwortlicherTSValidationGroup.class),
			not(violatesAnnotation(CheckVerantwortlicherTS.class)));
	}

	@Test
	void testCheckVerantwortlicherWrongSCHUser() {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(schAdmin);
		dossier.setVerantwortlicherTS(schAdmin);

		assertThat(
			validate(dossier, ChangeVerantwortlicherBGValidationGroup.class),
			violatesAnnotation(CheckVerantwortlicherBG.class));
		assertThat(
			validate(dossier, ChangeVerantwortlicherTSValidationGroup.class),
			not(violatesAnnotation(CheckVerantwortlicherTS.class)));
	}

	@Test
	void testCheckVerantwortlicherWrongJAUser() {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(jaAdmin);
		dossier.setVerantwortlicherTS(jaAdmin);

		assertThat(
			validate(dossier, ChangeVerantwortlicherBGValidationGroup.class),
			not(violatesAnnotation(CheckVerantwortlicherBG.class)));
		assertThat(
			validate(dossier, ChangeVerantwortlicherTSValidationGroup.class),
			violatesAnnotation(CheckVerantwortlicherTS.class));
	}
}
