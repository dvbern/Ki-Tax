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

import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.validators.CheckKinderabzugValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests fuer {@link CheckKinderabzugValidator}
 */
public class CheckKinderabzugValidatorTest {

	private CheckKinderabzugValidator validator;
	private Kind kind = new Kind();

	@Before
	public void setUp() {
		validator = new CheckKinderabzugValidator();
	}

	@Test
	public void checkPathPflegekindValid() {
		var kind = new Kind();
		kind.setPflegekind(true);
		kind.setPflegeEntschaedigungErhalten(true);
		Assert.assertTrue(validator.isValid(kind, null));
	}

	@Test
	public void checkPathPflegekindNotValid() {
		var kind = new Kind();
		kind.setPflegekind(true);
		kind.setPflegeEntschaedigungErhalten(true);
		kind.setObhutAlternierendAusueben(true);
		Assert.assertFalse(validator.isValid(kind, null));
	}

	@Test
	public void checkPathObhutalternierendValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setObhutAlternierendAusueben(true);
		kind.setGemeinsamesGesuch(false);
		Assert.assertTrue(validator.isValid(kind, null));
	}

	@Test
	public void checkPathObhutalternierendNotValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setObhutAlternierendAusueben(true);
		kind.setInErstausbildung(true);
		kind.setGemeinsamesGesuch(false);
		Assert.assertFalse(validator.isValid(kind, null));
	}

	@Test
	public void checkPathAlimenteBezahlenValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setInErstausbildung(true);
		kind.setLebtKindAlternierend(false);
		kind.setAlimenteBezahlen(true);
		Assert.assertTrue(validator.isValid(kind, null));
	}

	@Test
	public void checkPathAlimenteBezahlenNotValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setInErstausbildung(true);
		kind.setObhutAlternierendAusueben(true);
		kind.setLebtKindAlternierend(false);
		kind.setAlimenteBezahlen(true);
		Assert.assertFalse(validator.isValid(kind, null));
	}

	@Test
	public void checkPathAlimenteErhaltenValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setInErstausbildung(true);
		kind.setLebtKindAlternierend(true);
		kind.setAlimenteErhalten(true);
		Assert.assertTrue(validator.isValid(kind, null));
	}

	@Test
	public void checkPathAlimenteErhaltenNotValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setInErstausbildung(true);
		kind.setLebtKindAlternierend(true);
		kind.setAlimenteErhalten(true);
		kind.setAlimenteBezahlen(true);
		Assert.assertFalse(validator.isValid(kind, null));
	}

	@Test
	public void checkAsivPropertiesNotSet() {
		var kind = new Kind();
		kind.setPflegekind(true);
		kind.setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		Assert.assertFalse(validator.isValid(kind, null));

		kind = new Kind();
		kind.setObhutAlternierendAusueben(true);
		kind.setKinderabzugErstesHalbjahr(Kinderabzug.HALBER_ABZUG);
		Assert.assertFalse(validator.isValid(kind, null));

		kind = new Kind();
		kind.setInErstausbildung(true);
		kind.setKinderabzugErstesHalbjahr(Kinderabzug.HALBER_ABZUG);
		Assert.assertFalse(validator.isValid(kind, null));
	}

}
