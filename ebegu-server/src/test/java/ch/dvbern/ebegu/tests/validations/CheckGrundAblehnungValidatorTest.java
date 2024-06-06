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

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.validators.CheckGrundAblehnungValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests fuer CheckGrundAblehnungValidator
 */
class CheckGrundAblehnungValidatorTest {

	private CheckGrundAblehnungValidator validator;
	private Betreuung betreuung;

	@BeforeEach
	void setUp() {
		validator = new CheckGrundAblehnungValidator();
		betreuung = TestDataUtil.createDefaultBetreuung();
	}

	@ParameterizedTest(name = "#{index} - with args=<{0}>")
	@ValueSource(strings = {"", "mein Grund"})
	@NullSource
	void testNichtAbgewiesen(@Nullable String grund) {
		betreuung.setGrundAblehnung(grund);
		Assertions.assertTrue(validator.isValid(betreuung, null));
	}

	@ParameterizedTest(name = "#{index} - with args=<{0}>")
	@ValueSource(strings = "")
	@NullSource
	void testAbgewiesen(@Nullable String grund) {
		betreuung.setBetreuungsstatus(Betreuungsstatus.ABGEWIESEN);
		betreuung.setGrundAblehnung(grund);
		Assertions.assertFalse(validator.isValid(betreuung, null));
	}

	@Test
	void testAbgewiesenWithGrund() {
		betreuung.setBetreuungsstatus(Betreuungsstatus.ABGEWIESEN);
		betreuung.setGrundAblehnung("mein Grund");
		Assertions.assertTrue(validator.isValid(betreuung, null));
	}
}
