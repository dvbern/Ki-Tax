/*
 * Copyright (C) 2021 DV Bern AG,
 *  Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation,
 *  either version 3 of the
 * License,
 *  or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not,
 *  see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests.services;

import javax.ejb.EJBAccessException;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.RollenAbhaengigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.authentication.AuthorizerImpl;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;

@ExtendWith(EasyMockExtension.class)
public class AuthorizerUnitTest extends EasyMockSupport {

	@TestSubject
	private Authorizer authorizer = new AuthorizerImpl();

	@Mock
	private PrincipalBean principalMock;

	@Test
	public void readFallAllowedForMandant() {
		var fall = createFall();
		addMocksForFallAndReplay(fall.getMandant());
		authorizer.checkReadAuthorizationFall(fall);
	}

	@Test()
	public void readFallNotAllowedForMandant() {
		var fall = createFallLuzern();
		addMocksForFallAndReplay(TestDataUtil.getMandantKantonBern());
		Assertions.assertThrows(EJBAccessException.class, () -> authorizer.checkReadAuthorizationFall(fall));
	}

	@Test
	public void writeFallAllowedForMandant() {
		var fall = createFall();
		addMocksForFallAndReplay(fall.getMandant());
		authorizer.checkWriteAuthorization(fall);
	}

	@Test()
	public void writeFallNotAllowedForMandant() {
		var fall = createFallLuzern();
		addMocksForFallAndReplay(TestDataUtil.getMandantKantonBern());
		Assertions.assertThrows(EJBAccessException.class, () -> authorizer.checkWriteAuthorization(fall));
	}

	private Fall createFall() {
		return TestDataUtil.createDefaultFall();
	}

	private void addMocksForFallAndReplay(Mandant mandant) {
		expect(principalMock.getPrincipal()).andReturn(null);
		expect(principalMock.discoverRoles()).andReturn(null);
		expect(principalMock.isAnonymousSuperadmin()).andReturn(false);
		expect(principalMock.getMandant()).andReturn(mandant);
		expect(principalMock.isCallerInAnyOfRole(UserRole.ADMIN_MANDANT,
			UserRole.SACHBEARBEITER_MANDANT)).andReturn(true);
		expect(principalMock.isCallerInAnyOfRole(
			UserRole.SUPER_ADMIN,
			UserRole.ADMIN_BG,
			UserRole.SACHBEARBEITER_BG,
			UserRole.ADMIN_GEMEINDE,
			UserRole.SACHBEARBEITER_GEMEINDE,
			UserRole.ADMIN_TRAEGERSCHAFT,
			UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
			UserRole.ADMIN_INSTITUTION,
			UserRole.SACHBEARBEITER_INSTITUTION,
			UserRole.ADMIN_TS,
			UserRole.SACHBEARBEITER_TS,
			UserRole.STEUERAMT,
			UserRole.JURIST,
			UserRole.REVISOR,
			UserRole.ADMIN_MANDANT,
			UserRole.SACHBEARBEITER_MANDANT,
			UserRole.ADMIN_SOZIALDIENST,
			UserRole.SACHBEARBEITER_SOZIALDIENST
		)).andReturn(true);
		replayAll();
	}

	private void addMocksForZahlungenAndReplay(Mandant mandant) {
		expect(principalMock.discoverRoles()).andReturn(null);
		expect(principalMock.getPrincipal()).andReturn(null);
		expect(principalMock.isAnonymousSuperadmin()).andReturn(false);
		expect(principalMock.getMandant()).andReturn(mandant);
		expect(
			principalMock.isCallerInAnyOfRole(UserRole.getRolesWithoutAbhaengigkeit(RollenAbhaengigkeit.GEMEINDE))
		).andReturn(true);
		replayAll();
	}

	private Fall createFallLuzern() {
		var fall = createFall();
		fall.setMandant(TestDataUtil.getMandantLuzern());
		return fall;
	}

	@Test
	public void readZahlungsauftragAllowedForMandant() {
		addMocksForZahlungenAndReplay(TestDataUtil.getMandantLuzern());
		authorizer.checkReadAuthorizationZahlungsauftrag(createZahlungsauftragLuzern());
	}

	@Test
	public void readZahlungsauftragNotAllowedForMandant() {
		addMocksForZahlungenAndReplay(TestDataUtil.getMandantKantonBern());
		Assertions.assertThrows(EJBAccessException.class, () ->
			authorizer.checkReadAuthorizationZahlungsauftrag(createZahlungsauftragLuzern()));
	}

	@Test
	public void readZahlungAllowedForMandant() {
		addMocksForZahlungenAndReplay(TestDataUtil.getMandantLuzern());
		authorizer.checkReadAuthorizationZahlung(createZahlungLuzern());
	}

	@Test
	public void readZahlungNotAllowedForMandant() {
		addMocksForZahlungenAndReplay(TestDataUtil.getMandantKantonBern());
		Assertions.assertThrows(EJBAccessException.class, () ->
			authorizer.checkReadAuthorizationZahlung(createZahlungLuzern()));
	}

	private Zahlung createZahlungLuzern() {
		var zahlung = new Zahlung();
		zahlung.setZahlungsauftrag(createZahlungsauftragLuzern());
		return zahlung;
	}

	private Zahlungsauftrag createZahlungsauftragLuzern() {
		var zahlungsauftrag = new Zahlungsauftrag();
		var mandant = TestDataUtil.getMandantLuzern();
		var gemeinde = new Gemeinde();
		gemeinde.setMandant(mandant);
		zahlungsauftrag.setGemeinde(gemeinde);
		zahlungsauftrag.setMandant(mandant);
		return zahlungsauftrag;
	}

}
