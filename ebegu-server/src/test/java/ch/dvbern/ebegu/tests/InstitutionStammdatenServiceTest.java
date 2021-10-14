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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests fuer die Klasse PersonService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class InstitutionStammdatenServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private Persistence persistence;

	@Test
	public void createPersonInstitutionStammdatenTest() {
		Assert.assertNotNull(institutionStammdatenService);
		createAndPersistGesuchsperiode1718();
		InstitutionStammdaten insertedInstitutionStammdaten = insertInstitutionStammdaten();

		Collection<InstitutionStammdaten> allInstitutionStammdaten = institutionStammdatenService.getAllInstitutionStammdaten();
		Assert.assertEquals(1, allInstitutionStammdaten.size());
		InstitutionStammdaten nextInstitutionStammdaten = allInstitutionStammdaten.iterator().next();
		Assert.assertEquals(insertedInstitutionStammdaten.extractIban(), nextInstitutionStammdaten.extractIban());
		Assert.assertEquals(insertedInstitutionStammdaten.getBetreuungsangebotTyp(), nextInstitutionStammdaten.getBetreuungsangebotTyp());
	}

	@Test
	public void updateInstitutionStammdatenTest() {
		Assert.assertNotNull(institutionStammdatenService);
		createAndPersistGesuchsperiode1718();
		InstitutionStammdaten insertedInstitutionStammdaten = insertInstitutionStammdaten();

		Optional<InstitutionStammdaten> institutionStammdatenOptional = institutionStammdatenService.findInstitutionStammdaten(insertedInstitutionStammdaten.getId());
		Assert.assertTrue(institutionStammdatenOptional.isPresent());
		InstitutionStammdaten persistedInstStammdaten = institutionStammdatenOptional.get();
		Assert.assertEquals(insertedInstitutionStammdaten.extractIban(), persistedInstStammdaten.extractIban());

		Assert.assertNotNull(persistedInstStammdaten.getInstitutionStammdatenBetreuungsgutscheine());
		Assert.assertNotNull(persistedInstStammdaten.getInstitutionStammdatenBetreuungsgutscheine().getAuszahlungsdaten());
		persistedInstStammdaten.getInstitutionStammdatenBetreuungsgutscheine().getAuszahlungsdaten().setIban(new IBAN("CH39 0900 0000 3066 3817 2"));
		InstitutionStammdaten updatedInstitutionStammdaten = institutionStammdatenService.saveInstitutionStammdaten(persistedInstStammdaten);
		Assert.assertEquals(persistedInstStammdaten.extractIban(), updatedInstitutionStammdaten.extractIban());
	}

	@Test
	public void getAllInstitutionStammdatenByInstitution() {
		Assert.assertNotNull(institutionStammdatenService);
		createAndPersistGesuchsperiode1718();
		InstitutionStammdaten insertedInstitutionStammdaten = insertInstitutionStammdaten();
		String id = insertedInstitutionStammdaten.getInstitution().getId();
		InstitutionStammdaten stammdatenByInstitution = institutionStammdatenService.fetchInstitutionStammdatenByInstitution(id, true);
		Assert.assertNotNull(stammdatenByInstitution);
	}

	@Test
	public void updateBGInsitutionenGemeinden() {
		Assert.assertNotNull(institutionStammdatenService);
		createAndPersistGesuchsperiode1718();
		InstitutionStammdaten insertedInstitutionStammdaten = insertInstitutionStammdaten();
		String id = insertedInstitutionStammdaten.getInstitution().getId();
		institutionStammdatenService.updateGemeindeForBGInstitutionen();

		InstitutionStammdaten stammdatenByInstitution = institutionStammdatenService.fetchInstitutionStammdatenByInstitution(id, false);
		Assert.assertEquals("Bern", stammdatenByInstitution.getAdresse().getGemeinde());
		Assert.assertNotNull(stammdatenByInstitution.getAdresse().getBfsNummer());
		Assert.assertEquals(351, (long) stammdatenByInstitution.getAdresse().getBfsNummer());
	}

	// HELP METHODS

	private InstitutionStammdaten insertInstitutionStammdaten() {
		InstitutionStammdaten institutionStammdaten = TestDataUtil.createDefaultInstitutionStammdaten();
		Assert.assertNotNull(getDummySuperadmin().getMandant());
		institutionStammdaten.getInstitution().setMandant(getDummySuperadmin().getMandant());
		return TestDataUtil.saveInstitutionStammdatenIfNecessary(persistence, institutionStammdaten);
	}

	private InstitutionStammdaten addInstitutionsstammdaten(Institution institution, LocalDate start, LocalDate end) {
		InstitutionStammdaten institutionStammdaten = TestDataUtil.createDefaultInstitutionStammdaten();
		institutionStammdaten.setInstitution(institution);
		institutionStammdaten.getGueltigkeit().setGueltigAb(start);
		institutionStammdaten.getGueltigkeit().setGueltigBis(end);
		return institutionStammdatenService.saveInstitutionStammdaten(institutionStammdaten);
	}

	private void createAndPersistGesuchsperiode1718() {
		TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
	}

}
