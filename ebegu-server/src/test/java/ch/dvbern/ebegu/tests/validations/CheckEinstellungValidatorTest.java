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
	public void checkBegucreateTageMaxKita() {
		Assert.assertTrue(validator.isValid(createTageMaxKita(), null));
	}

	private Einstellung createTageMaxKita() {
		Einstellung einstellung = new Einstellung();
		einstellung.setKey(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA);
		einstellung.setValue("1");
		return einstellung;
	}
}
