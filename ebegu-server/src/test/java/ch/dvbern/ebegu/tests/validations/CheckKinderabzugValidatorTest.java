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
import ch.dvbern.ebegu.validators.CheckKinderabzugValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests fuer {@link CheckKinderabzugValidator}
 */
class CheckKinderabzugValidatorTest {

	private CheckKinderabzugValidator validator;

	@BeforeEach
	void setUp() {
		validator = new CheckKinderabzugValidator();
	}

	@Test
	void checkPathPflegekindValid() {
		var kind = new Kind();
		kind.setPflegekind(true);
		kind.setPflegeEntschaedigungErhalten(true);
		Assertions.assertTrue(validator.isValid(kind, null));
	}

	@Test
	void checkPathPflegekindNotValid() {
		var kind = new Kind();
		kind.setPflegekind(true);
		kind.setPflegeEntschaedigungErhalten(true);
		kind.setObhutAlternierendAusueben(true);
		Assertions.assertFalse(validator.isValid(kind, null));
	}

	@Test
	void checkPathObhutalternierendValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setObhutAlternierendAusueben(true);
		kind.setGemeinsamesGesuch(false);
		Assertions.assertTrue(validator.isValid(kind, null));
	}

	@Test
	void checkPathObhutalternierendNotValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setObhutAlternierendAusueben(true);
		kind.setInErstausbildung(true);
		kind.setGemeinsamesGesuch(false);
		Assertions.assertFalse(validator.isValid(kind, null));
	}

	@Test
	void checkPathAlimenteBezahlenValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setInErstausbildung(true);
		kind.setLebtKindAlternierend(false);
		kind.setAlimenteBezahlen(true);
		Assertions.assertTrue(validator.isValid(kind, null));
	}

	@Test
	void checkPathAlimenteBezahlenNotValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setInErstausbildung(true);
		kind.setObhutAlternierendAusueben(true);
		kind.setLebtKindAlternierend(false);
		kind.setAlimenteBezahlen(true);
		Assertions.assertFalse(validator.isValid(kind, null));
	}

	@Test
	void checkPathAlimenteErhaltenValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setInErstausbildung(true);
		kind.setLebtKindAlternierend(true);
		kind.setAlimenteErhalten(true);
		Assertions.assertTrue(validator.isValid(kind, null));
	}

	@Test
	void checkPathAlimenteErhaltenNotValid() {
		var kind = new Kind();
		kind.setPflegekind(false);
		kind.setInErstausbildung(true);
		kind.setLebtKindAlternierend(true);
		kind.setAlimenteErhalten(true);
		kind.setAlimenteBezahlen(true);
		Assertions.assertFalse(validator.isValid(kind, null));
	}

	@Test
	void checkPathKinderAbzugTypSchwyzKeinUnterhaltspflichtigNotValid() {
		var kind = new Kind();
		kind.setUnterhaltspflichtig(false);
		kind.setLebtKindAlternierend(true);
		Assertions.assertFalse(validator.isValid(kind, null));
	}

	@Test
	void checkPathKinderAbzugTypSchwyzUnterhaltspflichtigNotValid() {
		var kind = new Kind();
		kind.setUnterhaltspflichtig(true);
		kind.setLebtKindAlternierend(null);
		Assertions.assertFalse(validator.isValid(kind, null));
	}

	@Test
	void checkPathKinderAbzugTypSchwyzUnterhaltspflichtigValid() {
		var kind = new Kind();
		kind.setUnterhaltspflichtig(true);
		kind.setLebtKindAlternierend(true);
		Assertions.assertTrue(validator.isValid(kind, null));
	}

	@Test
	void checkPathKinderAbzugTypSchwyzKeinUnterhaltspflichtigValid() {
		var kind = new Kind();
		kind.setUnterhaltspflichtig(false);
		kind.setLebtKindAlternierend(null);
		Assertions.assertTrue(validator.isValid(kind, null));
	}
}
