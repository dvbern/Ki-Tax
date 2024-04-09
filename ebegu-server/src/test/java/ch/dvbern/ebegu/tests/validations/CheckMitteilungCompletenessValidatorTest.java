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

import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.validators.CheckMitteilungCompletenessValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests fuer {@link CheckMitteilungCompletenessValidator}
 */
public class CheckMitteilungCompletenessValidatorTest {

	CheckMitteilungCompletenessValidator validator;

	@BeforeEach
	public void setUp() {
		validator = new CheckMitteilungCompletenessValidator();
	}

	@Test
	public void testNeuNoMessageNoSubject() {
		Mitteilung mitteilung = new Mitteilung();
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		Assertions.assertFalse(validator.isValid(mitteilung, null));
	}

	@Test
	public void testNeuNoMessage() {
		Mitteilung mitteilung = new Mitteilung();
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		mitteilung.setSubject("subject");
		Assertions.assertFalse(validator.isValid(mitteilung, null));
	}

	@Test
	public void testNeuNoSubject() {
		Mitteilung mitteilung = new Mitteilung();
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		mitteilung.setMessage("message");
		Assertions.assertFalse(validator.isValid(mitteilung, null));
	}

	@Test
	public void testNeuComplete() {
		Mitteilung mitteilung = new Mitteilung();
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		mitteilung.setSubject("subject");
		mitteilung.setMessage("message");
		Assertions.assertTrue(validator.isValid(mitteilung, null));
	}

}
