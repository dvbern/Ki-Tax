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

package ch.dvbern.ebegu.rest.test;

import java.util.Optional;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionUpdate;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.TraegerschaftService;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

@RunWith(EasyMockRunner.class)
public class JaxBConverterInstitutionUpdateTest {

	@TestSubject
	private final JaxBConverter converter = new JaxBConverter();

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private PrincipalBean principalBeanMock;

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private TraegerschaftService traegerschaftServiceMock;

	@Test
	public void testInstitutionToEntity_falseWhenNothingChanged() {
		JaxInstitutionUpdate update = new JaxInstitutionUpdate();

		Institution institution = new Institution();
		institution.setStatus(InstitutionStatus.AKTIV);

		boolean actual = converter.institutionToEntity(update, institution, new InstitutionStammdaten());

		assertThat(actual, is(false));
	}

	@Test
	public void testInstitutionToEntity_trueWhenNameChanged() {
		JaxInstitutionUpdate update = new JaxInstitutionUpdate();
		update.setName("bar");

		Institution institution = new Institution();
		institution.setStatus(InstitutionStatus.AKTIV);
		institution.setName("foo");

		boolean actual = converter.institutionToEntity(update, institution, new InstitutionStammdaten());

		assertThat(actual, is(true));
		assertThat(institution.getName(), is("bar"));
	}

	@Test
	public void testInstitutionToEntity_trueWhenTraegerschaftChangedForSuperAdmin() {
		JaxInstitutionUpdate update = new JaxInstitutionUpdate();
		update.setTraegerschaftId("1");

		Institution institution = new Institution();
		institution.setStatus(InstitutionStatus.AKTIV);
		Traegerschaft traegerschaft = new Traegerschaft();
		institution.setTraegerschaft(traegerschaft);

		EasyMock.expect(principalBeanMock.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())).andReturn(true);

		Traegerschaft existingTraegerschaft = new Traegerschaft();
		EasyMock.expect(traegerschaftServiceMock.findTraegerschaft("1"))
			.andReturn(Optional.of(existingTraegerschaft));

		EasyMock.replay(principalBeanMock, traegerschaftServiceMock);

		boolean actual = converter.institutionToEntity(update, institution, new InstitutionStammdaten());

		assertThat(actual, is(true));
		assertThat(institution.getTraegerschaft(), is(sameInstance(existingTraegerschaft)));

		EasyMock.verify(principalBeanMock, traegerschaftServiceMock);
	}

	@Test
	public void testInstitutionToEntity_falseWhenNotSuperAdmin() {
		JaxInstitutionUpdate update = new JaxInstitutionUpdate();
		update.setTraegerschaftId("1");

		Institution institution = new Institution();
		institution.setStatus(InstitutionStatus.AKTIV);
		Traegerschaft traegerschaft = new Traegerschaft();
		institution.setTraegerschaft(traegerschaft);

		EasyMock.expect(principalBeanMock.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())).andReturn(false);

		EasyMock.replay(principalBeanMock);

		boolean actual = converter.institutionToEntity(update, institution, new InstitutionStammdaten());

		assertThat(actual, is(false));
		assertThat(institution.getTraegerschaft(), is(sameInstance(traegerschaft)));

		EasyMock.verify(principalBeanMock);
	}

	@Test
	public void testInstitutionToEntity_trueWhenTraegerschaftRemovedForSuperAdmin() {
		JaxInstitutionUpdate update = new JaxInstitutionUpdate();
		update.setTraegerschaftId(null);

		Institution institution = new Institution();
		institution.setStatus(InstitutionStatus.AKTIV);
		Traegerschaft traegerschaft = new Traegerschaft();
		traegerschaft.setId("2");
		institution.setTraegerschaft(traegerschaft);

		EasyMock.expect(principalBeanMock.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())).andReturn(true);

		EasyMock.replay(principalBeanMock);

		boolean actual = converter.institutionToEntity(update, institution, new InstitutionStammdaten());

		assertThat(actual, is(true));
		assertThat(institution.getTraegerschaft(), is(nullValue()));

		EasyMock.verify(principalBeanMock);
	}

	@Test
	public void testInstitutionToEntity_falseWhenTraegerschaftNotUpdatedForSuperAdmin() {
		JaxInstitutionUpdate update = new JaxInstitutionUpdate();
		update.setTraegerschaftId("1");

		Institution institution = new Institution();
		institution.setStatus(InstitutionStatus.AKTIV);
		Traegerschaft traegerschaft = new Traegerschaft();
		traegerschaft.setId("1");
		institution.setTraegerschaft(traegerschaft);

		EasyMock.expect(principalBeanMock.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())).andReturn(true);

		Traegerschaft existingTraegerschaft = new Traegerschaft();
		existingTraegerschaft.setId("1");
		EasyMock.expect(traegerschaftServiceMock.findTraegerschaft("1"))
			.andReturn(Optional.of(existingTraegerschaft));

		EasyMock.replay(principalBeanMock, traegerschaftServiceMock);

		boolean actual = converter.institutionToEntity(update, institution, new InstitutionStammdaten());

		assertThat(actual, is(false));
		assertThat(institution.getTraegerschaft(), is(sameInstance(traegerschaft)));

		EasyMock.verify(principalBeanMock, traegerschaftServiceMock);
	}
}
