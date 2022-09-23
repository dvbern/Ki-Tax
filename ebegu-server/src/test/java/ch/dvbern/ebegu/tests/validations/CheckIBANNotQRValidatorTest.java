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

import java.time.LocalDate;

import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.validators.CheckDateRangeValidator;
import ch.dvbern.ebegu.validators.CheckIBANNotQRValidator;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests fuer CheckDateRangeValidator
 */
public class CheckIBANNotQRValidatorTest {

	private CheckIBANNotQRValidator validator;

	@Before
	public void setUp() {
		validator = new CheckIBANNotQRValidator();
	}

	@Test
	public void test_shouldBeInvalidIfQRIBAN() {
		IBAN iban = new IBAN("CH4431999123000889012");
		Assert.assertFalse(validator.isValid(iban, null));
	}

	@Test
	public void test_shouldBeValidIfNormalIBAN() {
		IBAN iban = new IBAN("CH9300762011623852957");
		Assert.assertTrue(validator.isValid(iban, null));
	}

}
