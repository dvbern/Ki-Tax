/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests.validations;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.validators.CheckEinstellungValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests fuer {@link CheckEinstellungValidator}
 */
public class CheckEinstellungValidatorTest {

	private CheckEinstellungValidator validator;

	@Before
	public void setUp() {
		validator = new CheckEinstellungValidator();
	}

	@Test
	public void checkBeguBietenAbFirstOfMonth() {
		Assert.assertTrue(validator.isValid(createBeguBietenAb("2018-10-01"), null));
	}

	@Test
	public void checkBeguBietenAbInMonth() {
		Assert.assertFalse(validator.isValid(createBeguBietenAb("2018-10-15"), null));
	}

	@Test
	public void checkBegucreateTageMaxKita() {
		Assert.assertTrue(validator.isValid(createTageMaxKita(), null));
	}

	private Einstellung createBeguBietenAb(@Nonnull String date) {
		Einstellung einstellung = new Einstellung();
		einstellung.setKey(EinstellungKey.BEGU_ANBIETEN_AB);
		einstellung.setValue(date);
		return einstellung;
	}

	private Einstellung createTageMaxKita() {
		Einstellung einstellung = new Einstellung();
		einstellung.setKey(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA);
		einstellung.setValue("1");
		return einstellung;
	}
}
