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

import javax.ejb.EJBAccessException;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Einstellung;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(EasyMockExtension.class)
public class AbstractEntityListenerTest extends EasyMockSupport {

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private PrincipalBean principalBeanMock;

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private Principal principalMock;

	@Test
	public void isWriteAllowedIfAnonymousTest() {
		EasyMock.expect(principalBeanMock.getPrincipal()).andReturn(principalMock);
		EasyMock.expect(principalMock.getName()).andReturn(ANONYMOUS_USER_USERNAME);
		EasyMock.expect(principalBeanMock.isAnonymousSuperadmin()).andReturn(false);
		EasyMock.replay(principalMock, principalBeanMock);
		assertEquals(AbstractEntityListener.checkWriteAccessAllowedIfAnonymous(new Benutzer(), principalBeanMock), true);
	}

	@ParameterizedTest
	@MethodSource("anonymousNotAllowedClassProvider")
	public void isWriteAllowedIfAnonymousSuperadminTest(AbstractEntity entity) {
		EasyMock.expect(principalBeanMock.getPrincipal()).andReturn(principalMock);
		EasyMock.expect(principalMock.getName()).andReturn(ANONYMOUS_USER_USERNAME);
		EasyMock.expect(principalBeanMock.isAnonymousSuperadmin()).andReturn(true);
		EasyMock.replay(principalMock, principalBeanMock);
		assertEquals(AbstractEntityListener.checkWriteAccessAllowedIfAnonymous(entity, principalBeanMock), false);
	}

	@ParameterizedTest
	@MethodSource("anonymousNotAllowedClassProvider")
	public void isWriteNotAllowedIfAnonymousTest(AbstractEntity entity) {
		EasyMock.expect(principalBeanMock.getPrincipal()).andReturn(principalMock);
		EasyMock.expect(principalMock.getName()).andReturn(ANONYMOUS_USER_USERNAME);
		EasyMock.expect(principalBeanMock.isAnonymousSuperadmin()).andReturn(false);
		EasyMock.replay(principalMock, principalBeanMock);
		assertThrows(
			EJBAccessException.class,
			() -> AbstractEntityListener.checkWriteAccessAllowedIfAnonymous(entity, principalBeanMock));
	}

	@ParameterizedTest
	@MethodSource("anonymousAllowedClassProvider")
	public void isAccessAllowedIfAnonymousTest(AbstractEntity entity) {
		EasyMock.expect(principalBeanMock.getPrincipal()).andReturn(principalMock);
		EasyMock.expect(principalMock.getName()).andReturn(ANONYMOUS_USER_USERNAME);
		EasyMock.expect(principalBeanMock.isAnonymousSuperadmin()).andReturn(false);
		EasyMock.replay(principalMock, principalBeanMock);
		assertEquals(AbstractEntityListener.checkAccessAllowedIfAnonymous(entity, principalBeanMock), true);
	}

	@ParameterizedTest
	@MethodSource("anonymousNotAllowedClassProvider")
	public void isAccessAllowedIfAnonymousSuperadminTest(AbstractEntity entity) {
		EasyMock.expect(principalBeanMock.getPrincipal()).andReturn(principalMock);
		EasyMock.expect(principalMock.getName()).andReturn(ANONYMOUS_USER_USERNAME);
		EasyMock.expect(principalBeanMock.isAnonymousSuperadmin()).andReturn(true);
		EasyMock.replay(principalMock, principalBeanMock);
		assertEquals(AbstractEntityListener.checkAccessAllowedIfAnonymous(entity, principalBeanMock), false);
	}

	@ParameterizedTest
	@MethodSource("anonymousNotAllowedClassProvider")
	public void isAccessNotAllowedIfAnonymousTest(AbstractEntity entity) {
		EasyMock.expect(principalBeanMock.getPrincipal()).andReturn(principalMock);
		EasyMock.expect(principalMock.getName()).andReturn(ANONYMOUS_USER_USERNAME);
		EasyMock.expect(principalBeanMock.isAnonymousSuperadmin()).andReturn(false);
		EasyMock.replay(principalMock, principalBeanMock);
		assertThrows(
			EJBAccessException.class,
			() -> AbstractEntityListener.checkAccessAllowedIfAnonymous(entity, principalBeanMock));
	}

	private static Stream<Arguments> anonymousAllowedClassProvider() {
		return Stream.of(
			Arguments.of(new ApplicationProperty()),
			Arguments.of(new Gemeinde()),
			Arguments.of(new Mandant()),
			Arguments.of(new Benutzer())
		);
	}

	private static Stream<Arguments> anonymousNotAllowedClassProvider() {
		return Stream.of(
			Arguments.of(new Gesuch()),
			Arguments.of(new Fall()),
			Arguments.of(new Gesuchsperiode()),
			Arguments.of(new Einstellung())
		);
	}
}
