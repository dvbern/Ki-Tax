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

import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.enums.ExternalClientInstitutionType;
import ch.dvbern.ebegu.enums.ExternalClientType;
import ch.dvbern.ebegu.validators.ExternalClientOfType;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

class ExternalClientOfTypeValidatorTest extends AbstractValidatorTest {

	private static final ExternalClient VALID = new ExternalClient("foo", ExternalClientType.EXCHANGE_SERVICE_USER, ExternalClientInstitutionType.EXCHANGE_SERVICE_INSTITUTION);
	@SuppressWarnings("ConstantConditions")
	private static final ExternalClient INVALID = new ExternalClient("bar", null, ExternalClientInstitutionType.EXCHANGE_SERVICE_INSTITUTION);

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	void testMatchesType() {
		TestSingleProperty test = new TestSingleProperty(VALID);

		assertValid(test);
	}

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	void testViolateType() {
		TestSingleProperty test = new TestSingleProperty(INVALID);

		assertInvalid(test);
	}

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	void testMatchesTypeInSet() {
		TestSetProperty test = new TestSetProperty(Sets.newHashSet(VALID, VALID));

		assertValid(test);
	}

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	void testViolatesInvalidTypeInSet() {
		TestSetProperty test = new TestSetProperty(Sets.newHashSet(VALID, INVALID));

		assertInvalid(test);
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
