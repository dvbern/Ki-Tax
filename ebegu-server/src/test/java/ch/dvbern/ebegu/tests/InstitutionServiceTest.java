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

package ch.dvbern.ebegu.tests;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests fuer die Klasse InstitutionService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Transactional(TransactionMode.DISABLED)
public class InstitutionServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private InstitutionService institutionService;
	@Inject
	private InstitutionStammdatenService institutionStammdatenService;
	@Inject
	private Persistence persistence;

	@Test
	public void createInstitution() {
		Assert.assertNotNull(institutionService);
		Institution institution = insertInstitution().getInstitution();

		Optional<Institution> institutionOpt = institutionService.findInstitution(institution.getId(), true);
		assertTrue(institutionOpt.isPresent());
		final Institution foundInstitution = institutionOpt.get();

		assertEquals("Testinstitution", foundInstitution.getName());
		assertNotNull(institution.getMandant());
		assertNotNull(foundInstitution.getMandant());
		assertEquals(foundInstitution.getMandant().getId(), institution.getMandant().getId());

		assertNotNull(institution.getTraegerschaft());
		assertNotNull(foundInstitution.getTraegerschaft());
		assertEquals(foundInstitution.getTraegerschaft().getId(), institution.getTraegerschaft().getId());
	}

	@Test
	public void deleteInstitution() {
		Assert.assertNotNull(institutionService);
		Institution institution = insertInstitution().getInstitution();

		Optional<Institution> institutionOpt = institutionService.findInstitution(institution.getId(), true);
		assertTrue(institutionOpt.isPresent());

		institutionService.removeInstitution(institutionOpt.get().getId());

		Optional<Institution> institutionOpt2 = institutionService.findInstitution(institution.getId(), true);
		assertFalse(institutionOpt2.isPresent());
	}

	@Test
	public void getAllInstitutionenTest() {
		Assert.assertNotNull(institutionService);
		insertInstitution();

		Collection<Institution> allInstitutionen = institutionService.getAllInstitutionen();
		assertFalse(allInstitutionen.isEmpty());

	}

	// HELP METHODS

	private InstitutionStammdaten insertInstitution() {
		InstitutionStammdaten institutionStammdaten = TestDataUtil.createDefaultInstitutionStammdaten();
		final Institution institution = institutionStammdaten.getInstitution();

		persistence.persist(institution.getTraegerschaft());
		persistence.persist(institution.getMandant());

		final Institution persistedInstitution = institutionService.createInstitution(institution);
		institutionStammdaten.setInstitution(persistedInstitution);

		final InstitutionStammdaten persisted = persistence.persist(institutionStammdaten);

		return persisted;
	}

}
