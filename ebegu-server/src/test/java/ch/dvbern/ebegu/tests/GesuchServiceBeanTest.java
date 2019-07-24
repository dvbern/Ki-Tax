/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.tests;

import java.util.HashSet;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.mocks.CriteriaQueryHelperMock;
import ch.dvbern.ebegu.mocks.DossierServiceBeanMock;
import ch.dvbern.ebegu.mocks.FallServiceBeanMock;
import ch.dvbern.ebegu.mocks.PrincipalBeanMock;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.GesuchServiceBean;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.tests.util.UnitTestTempFolder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.needle4j.annotation.InjectIntoMany;
import org.needle4j.annotation.ObjectUnderTest;
import org.needle4j.junit.NeedleRule;

@SuppressWarnings("unused")
public class GesuchServiceBeanTest {

	@Rule
	public final UnitTestTempFolder unitTestTempfolder = new UnitTestTempFolder();

	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest
	private GesuchServiceBean gesuchService;

	@InjectIntoMany
	private PrincipalBean principalBean = new PrincipalBeanMock();

	@InjectIntoMany
	private DossierService dossierService = new DossierServiceBeanMock();

	@InjectIntoMany
	private CriteriaQueryHelper criteriaQueryHelper = new CriteriaQueryHelperMock();

	@InjectIntoMany
	private FallService fallService = new FallServiceBeanMock();


	@Test
	public void removeAntragAsGesuchstellerPapiergesuchNotAllowed() {
		// Als GS einloggen
		loginAs(UserRole.GESUCHSTELLER);
		// Papier-Erstgesuch
		Gesuch papierErstgesuch = TestDataUtil.createDefaultGesuch();
		papierErstgesuch.setEingangsart(Eingangsart.PAPIER);
		papierErstgesuch.setTyp(AntragTyp.ERSTGESUCH);
		try {
			gesuchService.removeAntrag(papierErstgesuch);
			Assert.fail("Exception erwartet. Gesuchsteller darf kein Papiergesuch löschen");
		} catch (EbeguRuntimeException e) {
			Assert.assertEquals(ErrorCodeEnum.ERROR_DELETION_NOT_ALLOWED_FOR_GS, e.getErrorCodeEnum());
		}
	}

	@Test
	public void removeAntragAsGesuchstellerFreigegebenNotAllowed() {
		// Als GS einloggen
		loginAs(UserRole.GESUCHSTELLER);
		// Online Gesuch, freigegeben
		Gesuch onlineGesuch = TestDataUtil.createDefaultGesuch();
		onlineGesuch.setEingangsart(Eingangsart.ONLINE);
		onlineGesuch.setStatus(AntragStatus.FREIGABEQUITTUNG);
		try {
			gesuchService.removeAntrag(onlineGesuch);
			Assert.fail("Exception erwartet. Gesuchsteller darf ein freigegebenes Gesuch nicht mehr löschen");
		} catch (EbeguRuntimeException e) {
			Assert.assertEquals(ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED, e.getErrorCodeEnum());
		}
	}

	@Test
	public void removeAntragAsGesuchstellerAllowed() {
		// Als GS einloggen
		loginAs(UserRole.GESUCHSTELLER);
		// Online Gesuch, freigegeben
		Gesuch onlineGesuch = TestDataUtil.createDefaultGesuch();
		onlineGesuch.setEingangsart(Eingangsart.ONLINE);
		onlineGesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		gesuchService.removeAntrag(onlineGesuch);
		Assert.assertFalse(gesuchService.findGesuch(onlineGesuch.getId()).isPresent());
	}

	@Test
	public void removeAntragAsAdminOnlineNotAllowed() {
		// Als Admin einloggen
		loginAs(UserRole.ADMIN_GEMEINDE);
		// Online Gesuch
		Gesuch onlineGesuch = TestDataUtil.createDefaultGesuch();
		onlineGesuch.setEingangsart(Eingangsart.ONLINE);
		try {
			gesuchService.removeAntrag(onlineGesuch);
			Assert.fail("Exception erwartet. Gemeinde darf ein online Gesuch nicht löschen");
		} catch (EbeguRuntimeException e) {
			Assert.assertEquals(ErrorCodeEnum.ERROR_DELETION_NOT_ALLOWED_FOR_JA, e.getErrorCodeEnum());
		}
	}

	@Test
	public void removeAntragAsAdminVerfuegtNotAllowed() {
		// Als Admin einloggen
		loginAs(UserRole.ADMIN_GEMEINDE);
		// Papier-Erstgesuch
		Gesuch papierErstgesuch = TestDataUtil.createDefaultGesuch();
		papierErstgesuch.setEingangsart(Eingangsart.PAPIER);
		papierErstgesuch.setStatus(AntragStatus.VERFUEGEN);
		try {
			gesuchService.removeAntrag(papierErstgesuch);
			Assert.fail("Exception erwartet. Gemeinde darf kein Gesuch löschen, das bereits im Status VERFUEGEN oder verfügt ist.");
		} catch (EbeguRuntimeException e) {
			Assert.assertEquals(ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED, e.getErrorCodeEnum());
		}
	}

	@Test
	public void removeAntragAsAdminAllowed() {
		// Als Admin einloggen
		loginAs(UserRole.ADMIN_GEMEINDE);
		// Online Gesuch, freigegeben
		Gesuch papierErstgesuch = TestDataUtil.createDefaultGesuch();
		papierErstgesuch.setEingangsart(Eingangsart.PAPIER);
		papierErstgesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		gesuchService.removeAntrag(papierErstgesuch);
		Assert.assertFalse(gesuchService.findGesuch(papierErstgesuch.getId()).isPresent());
	}

	private void loginAs(UserRole role) {
		Benutzer gesuchsteller = new Benutzer();
		gesuchsteller.setBerechtigungen(new HashSet<>());
		gesuchsteller.getBerechtigungen().add(new Berechtigung());
		gesuchsteller.setUsername("testuser");
		gesuchsteller.setRole(role);
		((PrincipalBeanMock)principalBean).setBenutzer(gesuchsteller);
	}
}
