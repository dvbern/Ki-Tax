/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.enums.ExternalClientInstitutionType;
import ch.dvbern.ebegu.enums.ExternalClientType;
import ch.dvbern.ebegu.tests.util.ValidationTestHelper;
import ch.dvbern.ebegu.validators.ExternalClientOfType;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

public class ExternalClientOfTypeValidatorTest {

	@SuppressWarnings("FieldCanBeLocal")
	private ValidatorFactory customFactory;

	private static final ExternalClient VALID = new ExternalClient("foo", ExternalClientType.EXCHANGE_SERVICE_USER, ExternalClientInstitutionType.EXCHANGE_SERVICE_INSTITUTION);
	@SuppressWarnings("ConstantConditions")
	private static final ExternalClient INVALID = new ExternalClient("bar", null, ExternalClientInstitutionType.EXCHANGE_SERVICE_INSTITUTION);

	@Before
	public void setUp() {
		// see https://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/chapter-bootstrapping
		// .html#_constraintvalidatorfactory
		Configuration<?> config = Validation.byDefaultProvider().configure();
		//wir verwenden dummy service daher geben wir hier null als em mit
		config.constraintValidatorFactory(new ValidationTestConstraintValidatorFactory());
		this.customFactory = config.buildValidatorFactory();
	}

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	public void testMatchesType() {
		TestSingleProperty test = new TestSingleProperty(VALID);

		ValidationTestHelper.assertNotViolated(test);
	}

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	public void testViolateType() {
		TestSingleProperty test = new TestSingleProperty(INVALID);

		ValidationTestHelper.assertViolated(test);
	}

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	public void testMatchesTypeInSet() {
		TestSetProperty test = new TestSetProperty(Sets.newHashSet(VALID, VALID));

		ValidationTestHelper.assertNotViolated(test);
	}

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	public void testViolatesInvalidTypeInSet() {
		TestSetProperty test = new TestSetProperty(Sets.newHashSet(VALID, INVALID));

		ValidationTestHelper.assertViolated(test);
	}

	private static final class TestSetProperty {
		private final Set<@ExternalClientOfType(type = ExternalClientType.EXCHANGE_SERVICE_USER) ExternalClient>
			clients;

		private TestSetProperty(Set<ExternalClient> clients) {
			this.clients = clients;
		}

		public Set<ExternalClient> getClients() {
			return clients;
		}
	}

	private static final class TestSingleProperty {
		@ExternalClientOfType(type = ExternalClientType.EXCHANGE_SERVICE_USER)
		private final ExternalClient client;

		public TestSingleProperty(ExternalClient client) {
			this.client = client;
		}

		public ExternalClient getClient() {
			return client;
		}
	}
}
