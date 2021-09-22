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

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.enums.FachstelleName;
import ch.dvbern.ebegu.services.FachstelleService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Arquillian Tests fuer die Klasse FachstelleService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Transactional(TransactionMode.DISABLED)
public class FachstelleServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private FachstelleService fachstelleService;

	@Inject
	private Persistence persistence;

	@Test
	public void createFachstelle() {
		assertNotNull(fachstelleService);
		Fachstelle fachstelle = TestDataUtil.createDefaultFachstelle();
		fachstelleService.saveFachstelle(fachstelle);

		Collection<Fachstelle> allFachstellen = fachstelleService.getAllFachstellen();
		assertEquals(1, allFachstellen.size());
		Fachstelle nextFamsit = allFachstellen.iterator().next();
		assertEquals(FachstelleName.DIENST_ZENTRUM_HOEREN_SPRACHE, nextFamsit.getName());
	}

	@Test
	public void updateFamiliensituationTest() {
		assertNotNull(fachstelleService);
		Fachstelle insertedFachstelle = insertNewEntity();
		Optional<Fachstelle> fachstelle = fachstelleService.findFachstelle(insertedFachstelle.getId());
		assertTrue(fachstelle.isPresent());
		assertEquals(FachstelleName.DIENST_ZENTRUM_HOEREN_SPRACHE, fachstelle.get().getName());

		fachstelle.get().setName(FachstelleName.FRUEHERZIEHUNG_BLINDENSCHULE_ZOLLIKOFEN);
		Fachstelle updatedFachstelle = fachstelleService.saveFachstelle(fachstelle.get());
		assertEquals(FachstelleName.FRUEHERZIEHUNG_BLINDENSCHULE_ZOLLIKOFEN, updatedFachstelle.getName());
		Optional<Fachstelle> fachstelleReRead = fachstelleService.findFachstelle(updatedFachstelle.getId());
		assertTrue(fachstelleReRead.isPresent());
		assertEquals(FachstelleName.FRUEHERZIEHUNG_BLINDENSCHULE_ZOLLIKOFEN, fachstelleReRead.get().getName());
	}

	@Test
	public void removeFachstelleTest() {
		assertNotNull(fachstelleService);
		Fachstelle insertedFachstelle = insertNewEntity();
		assertEquals(1, fachstelleService.getAllFachstellen().size());

		fachstelleService.removeFachstelle(insertedFachstelle.getId());
		assertEquals(0, fachstelleService.getAllFachstellen().size());
	}

	// HELP METHODS

	@Nonnull
	private Fachstelle insertNewEntity() {
		Fachstelle fachstelle = TestDataUtil.createDefaultFachstelle();
		TestDataUtil.saveMandantIfNecessary(persistence, fachstelle.getMandant());
		persistence.persist(fachstelle);
		return fachstelle;
	}
}
