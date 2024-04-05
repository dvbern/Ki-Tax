/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import ch.dvbern.ebegu.tests.util.validation.ViolationMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractValidatorTest {

	static ValidatorFactory validatorFactory = null;
	private static Validator validator = null;

	@BeforeAll
	@BeforeClass
	public static void createValidator() {
		// see https://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/chapter-bootstrapping.html#_constraintvalidatorfactory
		Configuration<?> config = Validation.byDefaultProvider().configure();
		//wir verwenden dummy service daher geben wir hier null als em mit
		config.constraintValidatorFactory(new ValidationTestConstraintValidatorFactory());
		validatorFactory = config.buildValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	@AfterClass
	public static void close() {
		validatorFactory.close();
	}

	@Nonnull
	protected <T> Set<ConstraintViolation<T>> validate(@Nonnull T var1, @Nonnull Class... var2) {
		return validator.validate(var1, var2);
	}

	protected <T> void assertValid(@Nonnull T var1, @Nonnull Class<?>... var2) {
		assertThat(validate(var1, var2), ViolationMatchers.succeeds());
	}

	protected <T> void assertInvalid(@Nonnull T var1, @Nonnull Class<?>... var2) {
		assertThat(validate(var1, var2), ViolationMatchers.fails());
	}
}
