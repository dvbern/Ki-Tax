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
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionUpdate;
import ch.dvbern.ebegu.api.resource.InstitutionResource;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

@RunWith(EasyMockRunner.class)
public class InstitutionResourceTest {

	@TestSubject
	private final InstitutionResource institutionResource = new InstitutionResource();

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private InstitutionService institutionService;

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private InstitutionStammdatenService institutionStammdatenService;

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private JaxBConverter converter;

	@Test
	public void testUpdateInstitutionAndStammdaten_checksInstitutionId() {
		String institutionId = "1";
		String stammdatenId = "2";
		String stammdatenInstitutonId = "3";

		JaxInstitutionStammdaten jaxStammdaten = new JaxInstitutionStammdaten();
		jaxStammdaten.setId(stammdatenId);

		JaxInstitutionUpdate jaxUpdate = new JaxInstitutionUpdate();
		jaxUpdate.setStammdaten(jaxStammdaten);

		Institution institution = new Institution();
		institution.setId(institutionId);
		EasyMock.expect(institutionService.findInstitution(institutionId, true))
			.andReturn(Optional.of(institution));

		// Stammdaten belog to another Institution
		Institution otherInstitution = new Institution();
		otherInstitution.setId(stammdatenInstitutonId);
		InstitutionStammdaten stammdaten = new InstitutionStammdaten(otherInstitution);
		EasyMock.expect(institutionStammdatenService.findInstitutionStammdaten(stammdatenId))
			.andReturn(Optional.of(stammdaten));

		EasyMock.replay(institutionService, institutionStammdatenService);

		try {
			//noinspection ResultOfMethodCallIgnored
			institutionResource.updateInstitutionAndStammdaten(new JaxId(institutionId), jaxUpdate);
			fail();
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), containsString("Stammdaten and Institution must belong together"));
		}

		EasyMock.verify(institutionService, institutionStammdatenService);
	}

	@Test
	public void testUpdateInstitutionAndStammdaten_firesChangeEventWithUpdatedStammdaten() {
		String institutionId = "1";

		Institution institution = new Institution();
		institution.setId(institutionId);
		EasyMock.expect(institutionService.findInstitution(institutionId, true))
			.andReturn(Optional.of(institution));

		converter.institutionStammdatenToEntity(EasyMock.anyObject(), EasyMock.anyObject());
		EasyMock.expectLastCall().once();

		EasyMock.expect(converter.institutionToEntity(EasyMock.anyObject(), EasyMock.anyObject(),
			EasyMock.anyObject()))
			.andReturn(false);

		EasyMock.expect(institutionStammdatenService.isGueltigkeitDecrease(EasyMock.anyObject(), EasyMock.anyObject()))
			.andReturn(false);

		InstitutionStammdaten updatedStammdaten = new InstitutionStammdaten();
		EasyMock.expect(institutionStammdatenService.saveInstitutionStammdaten(EasyMock.anyObject()))
			.andReturn(updatedStammdaten);

		// the event should be thrown with the updated Stammdaten
		institutionStammdatenService.fireStammdatenChangedEvent(EasyMock.eq(updatedStammdaten));
		EasyMock.expectLastCall().once();

		// the updated Stammdaten should be returned
		JaxInstitutionStammdaten expectedReturnValue = new JaxInstitutionStammdaten();
		EasyMock.expect(converter.institutionStammdatenToJAX(updatedStammdaten))
			.andReturn(expectedReturnValue);

		EasyMock.replay(institutionService, institutionStammdatenService, converter);

		JaxInstitutionStammdaten actualReturnValue =
			institutionResource.updateInstitutionAndStammdaten(new JaxId(institutionId), new JaxInstitutionUpdate());

		EasyMock.verify(institutionService, institutionStammdatenService, converter);

		assertThat(actualReturnValue, is(sameInstance(expectedReturnValue)));
	}
}
