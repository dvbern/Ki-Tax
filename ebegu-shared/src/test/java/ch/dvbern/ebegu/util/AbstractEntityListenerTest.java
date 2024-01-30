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

package ch.dvbern.ebegu.util;

import java.security.Principal;
import java.util.stream.Stream;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.ebegu.util.Constants.ANONYMOUS_USER_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EasyMockExtension.class)
public class AbstractEntityListenerTest extends EasyMockSupport {

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private PrincipalBean principalBeanMock;

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private Principal principalMock;

	@ParameterizedTest
	@MethodSource("classAndResultProvider")
	public void isAccessAllowedIfAnonymousTest(AbstractEntity entity, boolean expectedResult) {
		EasyMock.expect(principalBeanMock.getPrincipal()).andReturn(principalMock);
		EasyMock.expect(principalMock.getName()).andReturn(ANONYMOUS_USER_USERNAME);
		EasyMock.expect(principalBeanMock.isAnonymousSuperadmin()).andReturn(false);
		EasyMock.replay(principalMock, principalBeanMock);
		assertEquals(AbstractEntityListener.isAccessAllowedIfAnonymous(entity, principalBeanMock), expectedResult);
	}

	@Test
	public void isAccessAllowedIfAnonymousSuperadminTest() {
		EasyMock.expect(principalBeanMock.getPrincipal()).andReturn(principalMock);
		EasyMock.expect(principalMock.getName()).andReturn(ANONYMOUS_USER_USERNAME);
		EasyMock.expect(principalBeanMock.isAnonymousSuperadmin()).andReturn(true);
		EasyMock.replay(principalMock, principalBeanMock);
		assertEquals(AbstractEntityListener.isAccessAllowedIfAnonymous(new ApplicationProperty(), principalBeanMock), false);
	}

	private static Stream<Arguments> classAndResultProvider() {
		return Stream.of(
			Arguments.of(new ApplicationProperty(), true),
			Arguments.of(new Gemeinde(), true),
			Arguments.of(new Mandant(), true),
			Arguments.of(new Benutzer(), true),
			Arguments.of(new Gesuch(), false),
			Arguments.of(new Fall(), false),
			Arguments.of(new Gesuchsperiode(), false)
		);
	}
}
