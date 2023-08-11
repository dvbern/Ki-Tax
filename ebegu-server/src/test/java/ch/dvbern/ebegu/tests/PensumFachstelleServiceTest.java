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

import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.services.PensumFachstelleService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Tests fuer die Klasse PensumFachstelle
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Transactional(TransactionMode.DISABLED)
public class PensumFachstelleServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private PensumFachstelleService pensumFachstelleService;

	@Inject
	private Persistence persistence;

	@Test
	public void createPersonInstitutionStammdatenTest() {
		Assert.assertNotNull(pensumFachstelleService);
		PensumFachstelle insertedPensumFachstelle = insertPensumFachstelle();

		Optional<PensumFachstelle> returnedPensumFachstelle = pensumFachstelleService.findPensumFachstelle(insertedPensumFachstelle.getId());
		Assert.assertTrue(returnedPensumFachstelle.isPresent());
		Assert.assertNotNull(returnedPensumFachstelle.get().getId());
		Assert.assertNotNull(returnedPensumFachstelle.get().getTimestampErstellt());
		Assert.assertEquals(insertedPensumFachstelle.getFachstelle(), returnedPensumFachstelle.get().getFachstelle());
		Assert.assertEquals(insertedPensumFachstelle.getGueltigkeit(), returnedPensumFachstelle.get().getGueltigkeit());
		Assert.assertEquals(insertedPensumFachstelle.getPensum(), returnedPensumFachstelle.get().getPensum());
	}

	@Test
	public void updateInstitutionStammdatenTest() {
		Assert.assertNotNull(pensumFachstelleService);
		PensumFachstelle insertedPensumFachstelle = insertPensumFachstelle();

		Optional<PensumFachstelle> returnedPensumFachstelle = pensumFachstelleService.findPensumFachstelle(insertedPensumFachstelle.getId());
		Assert.assertTrue(returnedPensumFachstelle.isPresent());
		PensumFachstelle persistedPensFachstelle = returnedPensumFachstelle.get();
		Assert.assertEquals(insertedPensumFachstelle.getPensum(), persistedPensFachstelle.getPensum());

		insertedPensumFachstelle.setPensum(10);
		PensumFachstelle updatedPensumFachstelle = persistence.merge(insertedPensumFachstelle);
		Assert.assertNotEquals(insertedPensumFachstelle.getPensum(), persistedPensFachstelle.getPensum());
		Assert.assertEquals(insertedPensumFachstelle.getId(), persistedPensFachstelle.getId());
		Assert.assertEquals(insertedPensumFachstelle.getPensum(), updatedPensumFachstelle.getPensum());
	}

	// HELP METHODS

	private PensumFachstelle insertPensumFachstelle() {
		PensumFachstelle pensumFachstelle = TestDataUtil.createDefaultPensumFachstelle(null);
		TestDataUtil.persistFachstelle(persistence, pensumFachstelle.getFachstelle());
		persistence.persist(pensumFachstelle.getKind());
		return persistence.merge(pensumFachstelle);
	}

}

