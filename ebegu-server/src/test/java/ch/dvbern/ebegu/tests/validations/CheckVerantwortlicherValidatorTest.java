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

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.tests.util.ValidationTestHelper;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherBGValidationGroup;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherTSValidationGroup;
import ch.dvbern.ebegu.validators.CheckVerantwortlicherBG;
import ch.dvbern.ebegu.validators.CheckVerantwortlicherTS;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class CheckVerantwortlicherValidatorTest {

	private ValidatorFactory customFactory = null;
	private Benutzer schUser = null;
	private Benutzer jaUser = null;
	private Benutzer jaAdmin = null;
	private Benutzer schAdmin = null;

	@Before
	public void setUp() {
		Mandant mandant = TestDataUtil.createDefaultMandant();
		// see https://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/chapter-bootstrapping.html#_constraintvalidatorfactory
		Configuration<?> config = Validation.byDefaultProvider().configure();
		//wir verwenden dummy service daher geben wir hier null als em mit
		config.constraintValidatorFactory(new ValidationTestConstraintValidatorFactory(null));
		this.customFactory = config.buildValidatorFactory();
		schUser = TestDataUtil.createBenutzer(UserRole.SCHULAMT, "userSCH", null, null, mandant);
		jaUser = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_JA, "userJA", null, null, mandant);
		jaAdmin = TestDataUtil.createBenutzer(UserRole.ADMIN, "adminJA", null, null, mandant);
		schAdmin = TestDataUtil.createBenutzer(UserRole.ADMINISTRATOR_SCHULAMT, "adminSCH", null, null, mandant);
	}

	@Test
	public void testCheckVerantwortlicherNormalUsers() {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(jaUser);
		dossier.setVerantwortlicherTS(schUser);
		ValidationTestHelper.assertNotViolated(CheckVerantwortlicherBG.class, dossier, customFactory, ChangeVerantwortlicherBGValidationGroup.class);
		ValidationTestHelper.assertNotViolated(CheckVerantwortlicherTS.class, dossier, customFactory, ChangeVerantwortlicherTSValidationGroup.class);
	}

	@Test
	public void testCheckVerantwortlicherAdminUsers() {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(jaAdmin);
		dossier.setVerantwortlicherTS(schAdmin);
		ValidationTestHelper.assertNotViolated(CheckVerantwortlicherBG.class, dossier, customFactory, ChangeVerantwortlicherBGValidationGroup.class);
		ValidationTestHelper.assertNotViolated(CheckVerantwortlicherTS.class, dossier, customFactory, ChangeVerantwortlicherTSValidationGroup.class);
	}

	@Test
	public void testCheckVerantwortlicherNullUsers() {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(null);
		dossier.setVerantwortlicherTS(null);
		ValidationTestHelper.assertNotViolated(CheckVerantwortlicherBG.class, dossier, customFactory, ChangeVerantwortlicherBGValidationGroup.class);
		ValidationTestHelper.assertNotViolated(CheckVerantwortlicherTS.class, dossier, customFactory, ChangeVerantwortlicherTSValidationGroup.class);
	}

	@Test
	public void testCheckVerantwortlicherWrongSCHUser() {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(schAdmin);
		dossier.setVerantwortlicherTS(schAdmin);
		ValidationTestHelper.assertViolated(CheckVerantwortlicherBG.class, dossier, customFactory, ChangeVerantwortlicherBGValidationGroup.class);
		ValidationTestHelper.assertNotViolated(CheckVerantwortlicherTS.class, dossier, customFactory, ChangeVerantwortlicherTSValidationGroup.class);
	}

	@Test
	public void testCheckVerantwortlicherWrongJAUser() {
		final Dossier dossier = new Dossier();
		dossier.setVerantwortlicherBG(jaAdmin);
		dossier.setVerantwortlicherTS(jaAdmin);
		ValidationTestHelper.assertNotViolated(CheckVerantwortlicherBG.class, dossier, customFactory, ChangeVerantwortlicherBGValidationGroup.class);
		ValidationTestHelper.assertViolated(CheckVerantwortlicherTS.class, dossier, customFactory, ChangeVerantwortlicherTSValidationGroup.class);
	}

}
